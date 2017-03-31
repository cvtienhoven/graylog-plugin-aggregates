package org.graylog.plugins.aggregates.report.schedule;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.drools.core.time.impl.CronExpression;
import org.graylog.plugins.aggregates.report.schedule.rest.models.requests.AddReportScheduleRequest;
import org.graylog.plugins.aggregates.report.schedule.rest.models.requests.UpdateReportScheduleRequest;
import org.graylog.plugins.aggregates.rule.RuleImpl;
import org.graylog2.bindings.providers.MongoJackObjectMapperProvider;
import org.graylog2.database.CollectionName;
import org.graylog2.database.MongoConnection;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class ReportScheduleServiceImpl implements ReportScheduleService {

	private final JacksonDBCollection<ReportScheduleImpl, String> coll;
	private final JacksonDBCollection<RuleImpl, String> ruleColl;
	private final Validator validator;
	private static final Logger LOG = LoggerFactory.getLogger(ReportScheduleServiceImpl.class);

	@Inject
	public ReportScheduleServiceImpl(MongoConnection mongoConnection, MongoJackObjectMapperProvider mapperProvider,
			Validator validator) {
		
		this.validator = validator;

		final String collectionName = ReportScheduleImpl.class.getAnnotation(CollectionName.class).value();
		final DBCollection dbCollection = mongoConnection.getDatabase().getCollection(collectionName);
		this.coll = JacksonDBCollection.wrap(dbCollection, ReportScheduleImpl.class, String.class, mapperProvider.get());
		this.coll.createIndex(new BasicDBObject("name", 1), new BasicDBObject("unique", true));

		final String ruleCollectionName = RuleImpl.class.getAnnotation(CollectionName.class).value();
		final DBCollection ruleDbCollection = mongoConnection.getDatabase().getCollection(ruleCollectionName);
		this.ruleColl = JacksonDBCollection.wrap(ruleDbCollection, RuleImpl.class, String.class, mapperProvider.get());
		
		
		ReportSchedule defaultSchedule = null;
		
		try {
			defaultSchedule = coll.find(DBQuery.is("name", "Every Saturday, 23:59")).next();	
		} catch (Exception e) {
			LOG.debug("Default weekly ReportSchedule does not exist yet");
			defaultSchedule = coll.insert(ReportScheduleImpl.create(null, "Every Saturday, 23:59", "0 59 23 ? * SAT *", "P7D", true, null)).getSavedObject();
			LOG.debug("Created default weekly ReportSchedule with ID " + defaultSchedule.getId());
		}
				
		
		defaultSchedule = null;
		
		try {
			defaultSchedule = coll.find(DBQuery.is("name", "First day of month, 00:00")).next();	
		} catch (Exception e) {
			LOG.debug("Default monthly ReportSchedule does not exist yet");
			defaultSchedule = coll.insert(ReportScheduleImpl.create(null, "First day of month, 00:00", "0 0 0 1 1/1 ? *", "P1M", true, null)).getSavedObject();
			LOG.debug("Created default monthly ReportSchedule with ID " + defaultSchedule.getId());
		}

	}

	@Override
	public long count() {
		return coll.count();
	}

	@Override
	public ReportSchedule updateNextFireTime(String id, Date nextFireTime){
		LOG.info("updateNextFireTime() - _id=" + id + ", date=" +nextFireTime);
		DBUpdate.Builder update = new DBUpdate.Builder();
		update.set("nextFireTime", nextFireTime.getTime());
		BasicDBObject query = new BasicDBObject();
	    query.put("_id", new ObjectId(id));
		return coll.findAndModify(query, update);
	}
	
	@Override
	public ReportSchedule create(ReportSchedule schedule) {
		if (schedule instanceof ReportScheduleImpl) {
			final ReportScheduleImpl scheduleImpl = (ReportScheduleImpl) schedule;
			final Set<ConstraintViolation<ReportScheduleImpl>> violations = validator.validate(scheduleImpl);
						
			if (violations.isEmpty()) {
				CronExpression c;
				try {				
					c = new CronExpression(scheduleImpl.getExpression());
				} catch (ParseException e) {
					throw new IllegalArgumentException("Schedule " + scheduleImpl.getName() + " has invalid Cron Expression " + scheduleImpl.getExpression());
				}
				
				ReportSchedule reportSchedule = coll.insert(scheduleImpl).getSavedObject();
				
				LOG.info("created schedule with ID " + reportSchedule.getId());
				
				return reportSchedule;
			} else {
				throw new IllegalArgumentException("Specified object failed validation: " + violations);
			}
		} else
			throw new IllegalArgumentException(
					"Specified object is not of correct implementation type (" + schedule.getClass() + ")!");
	}
	
	@Override
	public ReportSchedule update(String name, ReportSchedule schedule) {		
		
		if (schedule instanceof ReportScheduleImpl) {
			final ReportScheduleImpl scheduleImpl = (ReportScheduleImpl) schedule;
			LOG.debug("updated schedule: " + scheduleImpl);
			final Set<ConstraintViolation<ReportScheduleImpl>> violations = validator.validate(scheduleImpl);
			if (violations.isEmpty()) {
				//return coll.update(DBQuery.is("name", name), ruleImpl, false, false).getSavedObject();
				return coll.findAndModify(DBQuery.is("name", name), new BasicDBObject(), new BasicDBObject(),
						false, scheduleImpl, true, false);
			} else {
				throw new IllegalArgumentException("Specified object failed validation: " + violations);
			}
		} else
			throw new IllegalArgumentException(
					"Specified object is not of correct implementation type (" + schedule.getClass() + ")!");
	}
	
	@Override
	public List<ReportSchedule> all() {		
		return toAbstractListType(coll.find());
	}

	@Override
	public ReportSchedule fromRequest(AddReportScheduleRequest request) {
		return ReportScheduleImpl.create(
				request.getReportSchedule().getId(),
				request.getReportSchedule().getName(), 
				request.getReportSchedule().getExpression(),
				request.getReportSchedule().getTimespan(),
				false,
				null);
	}

	@Override
	public ReportSchedule fromRequest(UpdateReportScheduleRequest request) {
		return ReportScheduleImpl.create(
				request.getReportSchedule().getId(),
				request.getReportSchedule().getName(), 
				request.getReportSchedule().getExpression(),
				request.getReportSchedule().getTimespan(),
				false,
				null);
	}
	
	@Override
	public int destroy(String id) {
		//Collection<String> idList = new ArrayList<String>();
		//idList.add(id);
		BasicDBObject query = new BasicDBObject();
	    query.put("reportSchedules", id);
		
		if (ruleColl.find(query).count() == 0){
			query = new BasicDBObject();
		    query.put("_id", new ObjectId(id));		    
			return coll.remove(query).getN();
		}
		throw new IllegalArgumentException(
				"There are still rules associated with schedule " + id );
		
	}

	private List<ReportSchedule> toAbstractListType(DBCursor<ReportScheduleImpl> rules) {
		return toAbstractListType(rules.toArray());
	}

	private List<ReportSchedule> toAbstractListType(List<ReportScheduleImpl> reportSchedules) {
		final List<ReportSchedule> result = Lists.newArrayListWithCapacity(reportSchedules.size());
		result.addAll(reportSchedules);

		return result;
	}

	@Override
	public ReportSchedule get(String id) {
		ReportSchedule reportSchedule = coll.find(DBQuery.is("_id", id)).next();
		return reportSchedule;
	}
}
