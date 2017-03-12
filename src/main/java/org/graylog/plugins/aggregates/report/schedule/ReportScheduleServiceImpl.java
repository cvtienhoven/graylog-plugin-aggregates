package org.graylog.plugins.aggregates.report.schedule;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import org.graylog.plugins.aggregates.report.schedule.rest.models.requests.AddReportScheduleRequest;
import org.graylog.plugins.aggregates.report.schedule.rest.models.requests.UpdateReportScheduleRequest;
import org.graylog.plugins.aggregates.rule.rest.models.requests.AddRuleRequest;
import org.graylog.plugins.aggregates.rule.rest.models.requests.UpdateRuleRequest;
import org.graylog2.bindings.providers.MongoJackObjectMapperProvider;
import org.graylog2.database.CollectionName;
import org.graylog2.database.MongoConnection;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;

public class ReportScheduleServiceImpl implements ReportScheduleService {

	private final JacksonDBCollection<ReportScheduleImpl, String> coll;
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
	}

	@Override
	public long count() {
		return coll.count();
	}

	@Override
	public ReportSchedule create(ReportSchedule schedule) {
		if (schedule instanceof ReportScheduleImpl) {
			final ReportScheduleImpl scheduleImpl = (ReportScheduleImpl) schedule;
			final Set<ConstraintViolation<ReportScheduleImpl>> violations = validator.validate(scheduleImpl);
			if (violations.isEmpty()) {
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
				request.getReportSchedule().getExpression());
	}

	@Override
	public ReportSchedule fromRequest(UpdateReportScheduleRequest request) {
		return ReportScheduleImpl.create(
				request.getReportSchedule().getId(),
				request.getReportSchedule().getName(), 
				request.getReportSchedule().getExpression());
	}
	
	@Override
	public int destroy(String ruleName) {
		return coll.remove(DBQuery.is("name", ruleName)).getN();
	}

	private List<ReportSchedule> toAbstractListType(DBCursor<ReportScheduleImpl> rules) {
		return toAbstractListType(rules.toArray());
	}

	private List<ReportSchedule> toAbstractListType(List<ReportScheduleImpl> rules) {
		final List<ReportSchedule> result = Lists.newArrayListWithCapacity(rules.size());
		result.addAll(rules);

		return result;
	}
}
