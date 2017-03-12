package org.graylog.plugins.aggregates.history;

import com.google.common.collect.Lists;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.mongodb.BasicDBObject;
import org.graylog2.bindings.providers.MongoJackObjectMapperProvider;
import org.graylog2.database.CollectionName;
import org.graylog2.database.MongoConnection;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.mongojack.Aggregation;
import org.mongojack.AggregationResult;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class HistoryItemServiceImpl implements HistoryItemService {
	private final static int SECONDS_IN_YEAR = 3600*24*366;
	private final static int SECONDS_IN_MONTH = 3600*24*31;
	private final static int SECONDS_IN_DAY = 3600*24;
	
	private final JacksonDBCollection<HistoryItemImpl, String> coll;
	private final Validator validator;
	private static final Logger LOG = LoggerFactory.getLogger(HistoryItemServiceImpl.class);

	@Inject
	public HistoryItemServiceImpl(MongoConnection mongoConnection, MongoJackObjectMapperProvider mapperProvider,
			Validator validator) {
		this.validator = validator;
		final String collectionName = HistoryItemImpl.class.getAnnotation(CollectionName.class).value();
		final DBCollection dbCollection = mongoConnection.getDatabase().getCollection(collectionName);
		this.coll = JacksonDBCollection.wrap(dbCollection, HistoryItemImpl.class, String.class, mapperProvider.get());
		// this.coll.createIndex(new BasicDBObject("name", 1), new
		// BasicDBObject("unique", true));
	}

	@Override
	public long count() {
		return coll.count();
	}

	@Override
	public HistoryItem create(HistoryItem historyItem) {
		if (historyItem instanceof HistoryItemImpl) {
			final HistoryItemImpl ruleImpl = (HistoryItemImpl) historyItem;
			final Set<ConstraintViolation<HistoryItemImpl>> violations = validator.validate(ruleImpl);
			if (violations.isEmpty()) {
				return coll.insert(ruleImpl).getSavedObject();

			} else {
				throw new IllegalArgumentException("Specified object failed validation: " + violations);
			}
		} else
			throw new IllegalArgumentException(
					"Specified object is not of correct implementation type (" + historyItem.getClass() + ")!");
	}

	@Override
	public List<HistoryItem> all() {
		return toAbstractListType(coll.find());
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<HistoryAggregateItem> getForRuleName(String ruleName, String timespan) {
		Period period = Period.parse(timespan);
		Duration duration = period.toDurationFrom(new DateTime());		
		int seconds = duration.toStandardSeconds().getSeconds();
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.SECOND, seconds * -1);
		DBObject project = null;
		BasicDBObject additionalOperation = null;
		
		if (seconds <= SECONDS_IN_DAY) {
			project = (DBObject) JSON.parse("{'$project': { 'datePartHour' : {'$concat' : [{'$substr' : [{'$year' : '$timestamp'}, 0, 4]}, '-', {'$substr' : [{'$month' : '$timestamp'}, 0, 2]}, '-', {'$substr' : [{'$dayOfMonth' : '$timestamp'}, 0, 2]}, 'T',  {'$substr' : [{'$hour' : '$timestamp'}, 0, 2]}] }, 'numberOfHits':'$numberOfHits'}}");
			additionalOperation = new BasicDBObject("$group", (new BasicDBObject("_id",
					"$datePartHour").append("moment", new BasicDBObject("$first","$datePartHour"))).append("numberOfHits", new BasicDBObject("$sum", "$numberOfHits")));
		} else if (seconds <= SECONDS_IN_MONTH){
			project = (DBObject) JSON.parse("{'$project': { 'datePartDay' : {'$concat' : [ {'$substr' : [{'$year' : '$timestamp'}, 0, 4]}, '-', {'$substr' : [{'$month' : '$timestamp'}, 0, 2]}, '-', {'$substr' : [{'$dayOfMonth' : '$timestamp'}, 0, 2]}] }, 'numberOfHits':'$numberOfHits'}}");
			additionalOperation = new BasicDBObject("$group", (new BasicDBObject("_id",
					"$datePartDay").append("moment", new BasicDBObject("$first","$datePartDay"))).append("numberOfHits", new BasicDBObject("$sum", "$numberOfHits")));
		} else if (seconds <= SECONDS_IN_YEAR) {

			project = (DBObject) JSON.parse(
					"{'$project': { 'datePartMonth' : {'$concat' : [ {'$substr' : [{'$year' : '$timestamp'}, 0, 4]}, '-', {'$substr' : [{'$month' : '$timestamp'}, 0, 2]}] }, 'numberOfHits':'$numberOfHits'}}");
			additionalOperation = new BasicDBObject("$group", (new BasicDBObject("_id",
					"$datePartMonth").append("moment", new BasicDBObject("$first","$datePartMonth"))).append("numberOfHits", new BasicDBObject("$sum", "$numberOfHits")));
		} else {

			project = (DBObject) JSON.parse(
					"{'$project': { 'datePartYear' : {'$concat' : [ {'$substr' : [{'$year' : '$timestamp'}, 0, 4]}] }, 'numberOfHits':'$numberOfHits'}}");
			additionalOperation = new BasicDBObject("$group", (new BasicDBObject("_id",
					"$datePartYear").append("moment", new BasicDBObject("$first","$datePartYear"))).append("numberOfHits", new BasicDBObject("$sum", "$numberOfHits")));
		}
		
		
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		
		//first match the records that meet the rule and the number of days 
		DBObject match = (DBObject) JSON.parse("{ '$match' : { $and: [ {'ruleName': '" + ruleName +"'}, {'timestamp': {'$gt' : { '$date': '" + df.format(c.getTime()) + "'}}}]}}");

		Aggregation<? extends HistoryAggregateItem> aggregation = new Aggregation<HistoryAggregateItemImpl>(HistoryAggregateItemImpl.class, match, project,
				additionalOperation);

		
		AggregationResult<? extends HistoryAggregateItem> aggregationResult = coll.aggregate(aggregation);

		LOG.debug("Aggregation result: " + aggregationResult.results().toString());
		
		return (List<HistoryAggregateItem>) aggregationResult.results();
				
	}	
	
	private List<HistoryItem> toAbstractListType(DBCursor<HistoryItemImpl> historyItems) {
		return toAbstractListType(historyItems.toArray());
	}

	private List<HistoryItem> toAbstractListType(List<HistoryItemImpl> historyItems) {
		final List<HistoryItem> result = Lists.newArrayListWithCapacity(historyItems.size());
		result.addAll(historyItems);
		LOG.debug("Number of history items returned: " + result.size());
		return result;
	}

	@Override
	public void removeBefore(Date date) {
		coll.remove(new BasicDBObject("timestamp", new BasicDBObject("$lt", date)));
		
	}

	@Override
	public List<HistoryAggregateItem> getForRuleName(String ruleName, int days) {
		// TODO Auto-generated method stub
		return null;
	}
}
