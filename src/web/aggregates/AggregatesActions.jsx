import Reflux from 'reflux';

const AggregatesActions = Reflux.createActions({
  list: { asyncResult: true },
  create: { asyncResult: true },
  deleteByName: { asyncResult: true },
  update: { asyncResult: true },
  createSchedule: { asyncResult: true },
  listSchedules: { asyncResult: true },
  deleteScheduleByName: { asyncResult: true },
  updateSchedule: { asyncResult: true },
});

export default AggregatesActions;
