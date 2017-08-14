import React from 'react';
import { Button } from 'react-bootstrap';
import Input from 'components/bootstrap/Input';
import BootstrapModalForm from 'components/bootstrap/BootstrapModalForm';
import ObjectUtils from 'util/ObjectUtils';
import TimespanConfiguration from './TimespanConfiguration';
import ValidationsUtils from 'util/ValidationsUtils';
import SchedulesActions from './SchedulesActions';

const EditScheduleModal = React.createClass({
  propTypes: {
    reportSchedule: React.PropTypes.object,
    create: React.PropTypes.bool,
    createReportSchedule: React.PropTypes.func.isRequired,
  },

  getDefaultProps() {
    return {
      reportSchedule: {
        name: '',
        expression: '* * * ? * *',
        timespan: 'P1D',
        reportReceivers: ''
      },
    };
  },
  getInitialState() {
    return {
      originalName: ObjectUtils.clone(this.props.reportSchedule).name,
      reportSchedule: ObjectUtils.clone(this.props.reportSchedule),
      create: ObjectUtils.clone(this.props.create),
      reportSchedules: [],
    };
  },
  componentDidMount() {

  },
  openModal() {
    this.refs.modal.open();
    this.setState(this.getInitialState());

    SchedulesActions.list().then(newReportSchedules => {
      this.setState({ reportSchedules: newReportSchedules });
    });

    const reportReceivers = this._reportReceiversToString(this.state.reportSchedule.reportReceivers);
    this.setState({ reportReceivers: reportReceivers });
  },

  _closeModal() {
    this.refs.modal.close();
  },

  _getId(prefixIdName) {
    return prefixIdName + this.state.reportSchedule.name;
  },
  _reportReceiversToString(reportReceivers) {
    let reportReceiversString = '';

    if (!reportReceivers || reportReceivers.length === 0) {
      return '';
    }

    for (let i = 0; i < reportReceivers.length; i++) {
      if (reportReceivers[i]) {
        reportReceiversString += reportReceivers[i].trim();

        if (i < reportReceivers.length - 1) {
          reportReceiversString += ',';
        }
      }
    }
    return reportReceiversString;
  },
  _saved() {
    this._closeModal();
    this.setState(this.getInitialState());
  },

  _save() {
    const reportSchedule = this.state.reportSchedule;

    if (this.state.originalName !== '') {
      this.props.createReportSchedule(this.state.originalName, reportSchedule, this._saved);
    } else {
      this.props.createReportSchedule(reportSchedule, this._saved);
    }
  },
  _onValueChanged(event) {
    const reportSchedule = this.state.reportSchedule;
    const parameter = event.target.name;
    const value = event.target.value;

    if (parameter === 'name') {
      if (this.props.create || (!this.props.create && value !== this.state.originalName)) {
        const nameField = this.refs.name.getInputDOMNode();
        const nameExists = this.state.reportSchedules.some(currentReportSchedule => currentReportSchedule.name === value);
        ValidationsUtils.setFieldValidity(nameField, nameExists, 'Schedule name is already taken');
      }
    }

    if (parameter === 'timespan') {
      const timespanField = this.refs.timespan.getInputDOMNode();
      const timespanValid = value > 0;

      ValidationsUtils.setFieldValidity(timespanField, timespanValid, 'Timespan should be greater than 0 days');
    }

    if (parameter === 'email') {
      const emailField = this.refs.email.getInputDOMNode();
      const emailArray = [];

      value.split(',').forEach(function (obj) {
        if (emailArray.indexOf(obj) === -1) emailArray.push(obj);
      });

      for (let i = 0; i < emailArray.length; i++) {
        if (emailArray[i]) {
          emailArray[i] = emailArray[i].trim();
        }
        const invalidEmail = !/^.+@.+\..+$/.test(emailArray[i]);
        ValidationsUtils.setFieldValidity(emailField, invalidEmail, `Email address ${emailArray[i]} is invalid`);
      }
      reportSchedule.reportReceivers = emailArray;
    }

    reportSchedule[parameter] = value.trim();

    this.setState({ reportSchedule: reportSchedule });
  },
  _setTimespan(timespan) {
    const reportSchedule = this.state.reportSchedule;
    reportSchedule.timespan = timespan.period;
    this.setState({ reportSchedule: reportSchedule });
  },
  render() {
    return (
      <span>
        <Button onClick={this.openModal}
          bsStyle={this.props.create ? 'success' : 'info'}
          bsSize={this.props.create ? null : 'xs'}
          disabled={this.state.reportSchedule.default}>
          {this.props.create ? 'Create Report Schedule' : 'Edit'}
        </Button>

        <BootstrapModalForm ref="modal"
          title="Edit Report Schedule"
          onSubmitForm={this._save}
          submitButtonText="Save">

          <fieldset>
            <Input ref="name" name="name" id="name" type="text" maxLength={100} defaultValue={this.state.originalName}
              labelClassName="col-sm-2" wrapperClassName="col-sm-10"
              label="Name" help="Enter a unique report schedule name." required
              onChange={this._onValueChanged} autoFocus />

            <Input ref="expression" name="expression" id="expression" type="text" maxLength={100} defaultValue={this.state.reportSchedule.expression}
              labelClassName="col-sm-2" wrapperClassName="col-sm-10"
              label="Cron Expression" help="Enter a cron expression using the Drools Cron Expression syntax. The first item (seconds) has to be supplied, but will be ignored as the analyzer runs once a minute." required
              onChange={this._onValueChanged} autoFocus />

            <TimespanConfiguration ref="timespan" name="timespan" id="timespan" config={this.state.reportSchedule ? { period: this.state.reportSchedule.timespan } : { period: 'P1D' }} updateConfig={this._setTimespan} />

            <Input ref="email" name="email" id="email" type="text" maxLength={500} defaultValue={this.state.reportReceivers}
                          labelClassName="col-sm-2" wrapperClassName="col-sm-10"
                          label="Email Receivers" help="Comma separated list of email addresses. Send a message to the addresses above when the alert condition was met."
                          onChange={this._onValueChanged} />

          </fieldset>
        </BootstrapModalForm>
      </span>
    );
  },
});

export default EditScheduleModal;
