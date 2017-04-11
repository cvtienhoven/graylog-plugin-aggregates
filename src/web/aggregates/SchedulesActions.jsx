import Reflux from 'reflux';

const SchedulesActions = Reflux.createActions({
  list: { asyncResult: true },
  create: { asyncResult: true },
  delete: { asyncResult: true },
  update: { asyncResult: true },
});

export default SchedulesActions;
