import Reflux from 'reflux';

const AggregatesActions = Reflux.createActions({
  list: { asyncResult: true },
  create: { asyncResult: true },
  deleteByName: { asyncResult: true },
  update: { asyncResult: true },
});

export default AggregatesActions;