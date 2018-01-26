import PropTypes from 'prop-types';
import React from 'react';
import { Button } from 'react-bootstrap';
import { BootstrapModalForm, Input } from 'components/bootstrap';
import { IfPermitted, Select } from 'components/common';
import { DocumentationLink } from 'components/support';
import TimespanConfiguration from './TimespanConfiguration';
import ObjectUtils from 'util/ObjectUtils';

const AggregatesConfig = React.createClass({
  propTypes: {
    config: PropTypes.object,
    updateConfig: PropTypes.func.isRequired,
  },

  getDefaultProps() {
    return {
      config: {
        purgeHistory: true,
        resolveOrphanedAlerts: false,
        historyRetention: 'P1M'
      },
    };
  },

  getInitialState() {
    return {
      config: ObjectUtils.clone(this.props.config),
    };
  },

  componentWillReceiveProps(newProps) {
    this.setState({config: ObjectUtils.clone(newProps.config)});
  },

  _updateConfigField(field, value) {
    const update = ObjectUtils.clone(this.state.config);
    update[field] = value;
    this.setState({config: update});
  },
  _updateHistoryRetention(timespan){
    const update = ObjectUtils.clone(this.state.config);
    update.historyRetention = timespan.period;
    this.setState({config: update});
  },
  _onCheckboxClick(field, ref) {
    return () => {
      this._updateConfigField(field, this.refs[ref].getChecked());
    };
  },

  _onSelect(field) {
    return (selection) => {
      this._updateConfigField(field, selection);
    };
  },

  _onUpdate(field) {
    return (e) => {
      this._updateConfigField(field, e.target.value);
    };
  },

  _openModal() {
    this.refs.aggregatesModal.open();
  },

  _closeModal() {
    this.refs.aggregatesModal.close();
  },

  _resetConfig() {
    // Reset to initial state when the modal is closed without saving.
    this.setState(this.getInitialState());
  },

  _saveConfig() {
    this.props.updateConfig(this.state.config).then(() => {
      this._closeModal();
    });
  },

  render() {
    return (
      <div>
        <h3>Aggregates Plugin</h3>

        <p>
          With the Aggregates Plugin you can define aggregate rules with criteria for a terms search,
          so you can generate an alert when the same value for field X occurs Y times in a Z minute time frame.
        </p>

        <dl className="deflist">
          <dt>Purge History:</dt>
          <dd>{this.state.config.purgeHistory === true ? 'yes' : 'no'}</dd>
          <dt>History Retention:</dt>
          <dd>{this.state.config.historyRetention}</dd>
          <dt>Resolve Orphaned Alerts:</dt>
          <dd>{this.state.config.resolveOrphanedAlerts === true ? 'yes' : 'no'}</dd>
        </dl>

        <IfPermitted permissions="clusterconfigentry:edit">
          <Button bsStyle="info" bsSize="xs" onClick={this._openModal}>Update</Button>
        </IfPermitted>

        <BootstrapModalForm ref="aggregatesModal"
                            title="Update Aggregates Plugin Configuration"
                            onSubmitForm={this._saveConfig}
                            onModalClose={this._resetConfig}
                            submitButtonText="Save">
          <fieldset>
            <Input id="purge-history-checkbox"
                   type="checkbox"
                   ref="purgeHistory"
                   label="Purge historic hits beyond the either retention period or the longest report interval (automatically determined).
                          Recommended to leave enabled to prevent MongoDB filling up."
                   name="purgeHistory"
                   checked={this.state.config.purgeHistory}
                   onChange={this._onCheckboxClick('purgeHistory', 'purgeHistory')}/>

            <TimespanConfiguration ref="historyRetention" name="historyRetention" id="historyRetention" config={this.state.config.historyRetention ? { period: this.state.config.historyRetention } : { period: 'P1M' }} updateConfig={this._updateHistoryRetention} />

            <Input id="resolve-orphaned-alerts-checkbox"
                   type="checkbox"
                   ref="resolveOrphanedAlerts"
                   label="Resolve orphaned Aggregate alerts, remove orphaned Aggregate Alert Conditions. Only needed when alerts with Unknown Alert title appear."
                   name="resolveOrphanedAlerts"
                   checked={this.state.config.resolveOrphanedAlerts}
                   onChange={this._onCheckboxClick('resolveOrphanedAlerts', 'resolveOrphanedAlerts')}/>
          </fieldset>
        </BootstrapModalForm>
      </div>
    );
  },
});

export default AggregatesConfig;