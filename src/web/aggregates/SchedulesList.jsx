import React from 'react';
import Reflux from 'reflux';
import SchedulesStore from './SchedulesStore';
import SchedulesActions from './SchedulesActions';
import EditScheduleModal from './EditScheduleModal';
import { DataTable, Spinner, IfPermitted } from 'components/common';

const SchedulesList = React.createClass({
  mixins: [Reflux.connect(SchedulesStore)],
  getInitialState() {
    return {
      reportSchedules: undefined,
    };
  },
  componentDidMount() {
    this.list();
  },
  _editSchedule(originalName, reportSchedules, callback) {
    SchedulesActions.update.triggerPromise(originalName, reportSchedules)
      .then(() => {
        callback();
        return null;
      });
  },
  list() {
    SchedulesActions.list().then(newSchedules => {
      this.setState({ reportSchedules: newSchedules });
    });
  },
  delete(id) {
    SchedulesActions.delete(id);
  },
  _deleteScheduleFunction(id, name) {
    return () => {
      if (window.confirm(`Do you really want to delete schedule ${name}?`)) {
        this.delete(id);
      }
    };
  },
  _headerCellFormatter(header) {
    let formattedHeaderCell;

    switch (header.toLocaleLowerCase()) {
      case '':
        formattedHeaderCell = <th className="user-type">{header}</th>;
        break;
      case 'actions':
        formattedHeaderCell = <th className="actions">{header}</th>;
        break;
      default:
        formattedHeaderCell = <th>{header}</th>;
    }

    return formattedHeaderCell;
  },
  _alertReceiversFormatter(reportSchedule) {
    if (reportSchedule.reportReceivers !== null) {
      const emailReceivers = reportSchedule.reportReceivers.map((receiver) => {
        return (
          <li key={receiver}>
            <i className="fa fa-envelope"/> {receiver}
          </li>
        );
      });
      return (
        <ul className="alert-receivers">
          {emailReceivers}
        </ul>
      );
    } else {
      return (
        <ul className="alert-receivers">
        </ul>
      )
    }
  },
  _scheduleInfoFormatter(reportSchedule) {
    const deleteAction = (
      <IfPermitted permissions="aggregate_report_schedules:delete">
        <button id="delete-reportSchedule" type="button" className="btn btn-xs btn-primary" title="Delete schedule"
              onClick={this._deleteScheduleFunction(reportSchedule._id, reportSchedule.name)} disabled={reportSchedule.default}>
          Delete
        </button>
      </IfPermitted>
    );

    const editAction = (
      <IfPermitted permissions="aggregate_report_schedules:update">
        <EditScheduleModal create={false} createReportSchedule={this._editSchedule} reportSchedule={reportSchedule}/>
      </IfPermitted>
    );

    const actions = (
      <div>
        {deleteAction}
        &nbsp;
        {editAction}
      </div>
    );

    return (
      <tr key={reportSchedule.name}>
        <td className="limited">{reportSchedule.name}</td>
        <td className="limited">{reportSchedule.expression}</td>
        <td className="limited">{reportSchedule.timespan}</td>
        <td className="limited">{reportSchedule.nextFireTime != null ? new Date(reportSchedule.nextFireTime).toString() : 'unknown'}</td>
        <td className="limited">{this._alertReceiversFormatter(reportSchedule)}</td>
        <td>{actions}</td>
      </tr>
    );
  },
  render() {
    const filterKeys = ['name'];
    const headers = ['Schedule name', 'Cron expression', 'Timespan', 'Next fire time', "Report receiver(s)"];

    if (this.state.reportSchedules) {
      return (
        <div>
          <DataTable id="schedule-list"
                     className="table-hover"
                     headers={headers}
                     headerCellFormatter={this._headerCellFormatter}
                     sortByKey={'name'}
                     rows={this.state.reportSchedules}
                     filterBy="field"
                     dataRowFormatter={this._scheduleInfoFormatter}
                     filterLabel="Filter Rules"
                     filterKeys={filterKeys}/>
        </div>
      );
    }

    return <Spinner />;
  },
});

export default SchedulesList;
