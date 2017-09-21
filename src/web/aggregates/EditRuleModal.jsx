import React from 'react';
import Reflux from 'reflux';
import AggregatesStore from './AggregatesStore';
import { Button } from 'react-bootstrap';
import Input from 'components/bootstrap/Input';
import MultiSelect from 'components/common/MultiSelect';
import BootstrapModalForm from 'components/bootstrap/BootstrapModalForm';
import ObjectUtils from 'util/ObjectUtils';
import ValidationsUtils from 'util/ValidationsUtils';
import AggregatesActions from './AggregatesActions';
import SchedulesActions from './SchedulesActions';
import { IfPermitted } from 'components/common';
import StoreProvider from 'injection/StoreProvider';
import CombinedProvider from 'injection/CombinedProvider';

const StreamsStore = StoreProvider.getStore('Streams');

const EditRuleModal = React.createClass({
  mixins: [Reflux.connect(AggregatesStore)],
  propTypes: {
    rule: React.PropTypes.object,
    create: React.PropTypes.bool,
    createRule: React.PropTypes.func.isRequired,
  },

  getDefaultProps() {
    return {
      rule: {
        name: '',
        field: '',
        matchMoreOrEqual: true,
        numberOfMatches: 1,
        interval: 1,
        streamId: '',
        notificationId: '',
        inReport: true,
        reportSchedules: [],
        repeatNotifications: true,
      },
    };
  },

  getInitialState() {
    return {
      originalName: ObjectUtils.clone(this.props.rule).name,
      rule: ObjectUtils.clone(this.props.rule),
      create: ObjectUtils.clone(this.props.create),
      rules: [],
      streams: [],
      alerts: [],
      reportSchedules: [],
      selectedReportSchedules: [],
    };
  },
  componentDidMount() {
    this.setState(this.getInitialState());
  },


  openModal() {


    StreamsStore.listStreams().then(list => {
      list.sort(function (a, b) {
        if (a.title.toLowerCase() > b.title.toLowerCase()) {
          return 1;
        }
        if (a.title.toLowerCase() < b.title.toLowerCase()) {
          return -1;
        }
        // a must be equal to b
        return 0;
      });
      this.setState({ streams: list });
    });

    const selectedReportSchedules = (this.state.rule.reportSchedules === null ? [] : this.state.rule.reportSchedules);

    this.setState({ selectedReportSchedules: selectedReportSchedules });

    AggregatesActions.list().then(newRules => {
      this.setState({ rules: newRules });
    });

    SchedulesActions.list().then(newReportSchedules => {
      this.setState({ reportSchedules: newReportSchedules });
    });



    this.refs.modal.open();
  },
  _closeModal() {
    this.refs.modal.close();
  },
  _getId(prefixIdName) {
    return prefixIdName + this.state.rule.name;
  },
  _saved() {
    this._closeModal();
  },
  _save() {
    const rule = this.state.rule;

    rule.reportSchedules = this.state.selectedReportSchedules;

    if (!rule.alertReceivers) {
      rule.alertReceivers = [];
    }

    if (this.state.originalName !== '') {
      this.props.createRule(this.state.originalName, rule, this._saved);
    } else {
      this.props.createRule(rule, this._saved);
    }
  },
  _createStreamSelectItems() {
    const items = [];

    for (let i = 0; i < this.state.streams.length; i++) {
      items.push(<option key={i} value={this.state.streams[i].id}>{this.state.streams[i].title}</option>);
    }

    return items;
  },
  _onValueChanged(event) {
    const rule = this.state.rule;

    const parameter = event.target.name;
    const value = event.target.type === 'checkbox' ? event.target.checked : event.target.value;

    if (parameter === 'name') {
      if (!this.props.create && name !== this.state.originalName) {
        const nameField = this.refs.name.getInputDOMNode();
        const nameExists = this.state.rules.some(currentRule => currentRule.name === event.target.value);
        ValidationsUtils.setFieldValidity(nameField, nameExists, 'Rule name is already taken');
      }
    }

    if (parameter === 'interval') {
      const intervalField = this.refs.interval.getInputDOMNode();
      const intervalValue = value < 1;
      ValidationsUtils.setFieldValidity(intervalField, intervalValue, 'Interval should be at least 1');
    }

    if (parameter === 'numberOfMatches') {
      const numberOfMatchesField = this.refs.numberOfMatches.getInputDOMNode();
      const numberOfMatchesValue = value < 1;
      ValidationsUtils.setFieldValidity(numberOfMatchesField, numberOfMatchesValue, 'Number of matches should be at least 1');
    }



    if (parameter === 'sliding' || parameter === 'repeatNotifications') {
      rule[parameter] = value;
    } else {
      rule[parameter] = value.trim();
    }

    this.setState({ rule: rule });
  },
  formatMultiselectOptions(collection) {
    return collection.map((item) => {
      return { key: item._id, value: item._id, label: item.name };
    });
  },
  formatSelectedOptions(collection) {
    return collection.map((item) => item._id).join(',');
  },
  _setReportSchedules(suppliedReportSchedules) {
    if (suppliedReportSchedules === '') {
      this.setState({ selectedReportSchedules: [] });
    } else {
      this.setState({ selectedReportSchedules: suppliedReportSchedules.split(',') });
    }
  },
  render() {
    const reportScheduleOptions = this.formatMultiselectOptions(this.state.reportSchedules);

    return (
      <span>
        <Button onClick={this.openModal}
                bsStyle={this.props.create ? 'success' : 'info'}
                bsSize={this.props.create ? null : 'xs'}
                type="submit">
          {this.props.create ? 'Create rule' : 'Edit'}
        </Button>
        <BootstrapModalForm ref="modal"
          title={`${this.props.create ? 'Create' : 'Edit'} Rule ${this.state.originalName}`}
          onSubmitForm={this._save}
          submitButtonText="Save">

          <fieldset>
            <Input ref="name" name="name" id="name" type="text" maxLength={100} defaultValue={this.state.originalName}
              labelClassName="col-sm-2" wrapperClassName="col-sm-10"
              label="Name" help="Enter a unique rule name." required
              onChange={this._onValueChanged} autoFocus />

            <Input ref="streamId" name="streamId" id="streamId" type="select" value={this.state.rule.streamId}
              labelClassName="col-sm-2" wrapperClassName="col-sm-10"
              label="Stream" help="Select a stream." required
              onChange={this._onValueChanged} > {this._createStreamSelectItems()}
            </Input>

            <Input ref="query" name="query" id="query" type="text" maxLength={400} defaultValue={this.state.rule.query}
              labelClassName="col-sm-2" wrapperClassName="col-sm-10"
              label="Query" help="Execute this query..." required
              onChange={this._onValueChanged} />

            <Input ref="field" name="field" id="field" type="text" maxLength={50} defaultValue={this.state.rule.field}
              labelClassName="col-sm-2" wrapperClassName="col-sm-10"
              label="Field" help="...and check if a value of the above field occurs..." required
              onChange={this._onValueChanged} />

            <Input ref="matchMoreOrEqual" name="matchMoreOrEqual" id="matchMoreOrEqual" type="select" defaultValue={this.state.rule.matchMoreOrEqual}
              labelClassName="col-sm-2" wrapperClassName="col-sm-10"
              label="Match more or equal" help="...than... " required
              onChange={this._onValueChanged} >
              <option value="true">More or equal</option>
              <option value="false">Less</option>
            </Input>

            <Input ref="numberOfMatches" name="numberOfMatches" id="numberOfMatches" type="number" maxLength={5} defaultValue={this.state.rule.numberOfMatches}
              labelClassName="col-sm-2" wrapperClassName="col-sm-10"
              label="#Matches" help="...times in a..." required
              onChange={this._onValueChanged} />

            <Input ref="interval" name="interval" id="interval" type="number" maxLength={5} defaultValue={this.state.rule.interval}
              labelClassName="col-sm-2" wrapperClassName="col-sm-10"
              label="Interval (minutes)" help="...minute interval." required
              onChange={this._onValueChanged} />

            <Input key="sliding" ref="sliding" name="sliding" id="sliding" type="checkbox" checked={this.state.rule.sliding}
              label="Evaluate every minute" labelClassName="col-sm-12" wrapperClassName="col-sm-offset-2 col-sm-10"
              help="When checked, the rule will be evaluated every minute, else it will be evaluated every <interval> minute(s). Enabling could result in more alerts."
              onChange={this._onValueChanged} />

            <Input key="repeatNotifications" ref="repeatNotifications" name="repeatNotifications" id="repeatNotifications" type="checkbox" checked={this.state.rule.repeatNotifications}
               label="Repeat notifications" labelClassName="col-sm-12" wrapperClassName="col-sm-offset-2 col-sm-10"
               help="Check this box to send notifications every time the alert condition is evaluated and satisfied regardless of its state."
               onChange={this._onValueChanged} />

            <Input id="schedules" labelClassName="col-sm-2"
                          wrapperClassName="col-sm-10" label="Report Schedule(s)">

                          <MultiSelect
                            ref="select"
                            options={reportScheduleOptions}
                            value={this.state.selectedReportSchedules.join(',')}
                            onChange={this._setReportSchedules}
                            placeholder="Choose report schedules..."
                          />
                        </Input>
          </fieldset>
        </BootstrapModalForm>
      </span>
    );
  },
});

export default EditRuleModal;
