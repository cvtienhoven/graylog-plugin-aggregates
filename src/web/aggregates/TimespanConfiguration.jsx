import React from 'react';
import Input from 'components/bootstrap/Input';
import moment from 'moment';

const TimespanConfiguration = React.createClass({
  propTypes: {
    config: React.PropTypes.object.isRequired,
    updateConfig: React.PropTypes.func.isRequired,
  },

  getInitialState() {
    return {
      period: this.props.config.period,
    };
  },
  _onPeriodUpdate(field) {
    return () => {
      const update = {};
      let period = this.refs[field].getValue().toUpperCase();

      if (!period.startsWith('P')) {
        period = `P${period}`;
      }

      update[field] = period;

      this.setState(update);

      if (this._isValidPeriod(update[field])) {
        // Only propagate state if the config is valid.
        this.props.updateConfig(update);
      }
    };
  },
  _isValidPeriod(duration) {
    const check = duration || this.state.period;
    return moment.duration(check).asMilliseconds() >= 3600000;
  },
  _validationState() {
    if (this._isValidPeriod()) {
      return undefined;
    }
    return 'error';
  },
  _formatDuration() {
    return this._isValidPeriod() ? moment.duration(this.state.period).humanize() : 'invalid (min 1 hour)';
  },

  render() {
    return (
      <div>
        <Input type="text"
               ref="period"
               label="Timespan (ISO8601 Duration)"
               onChange={this._onPeriodUpdate('period')}
               value={this.state.period}
               help={'How long the history has to be to incorporate into the report. (i.e. "P1D" for 1 day, "PT6H" for 6 hours)'}
               addonAfter={this._formatDuration()}
               bsStyle={this._validationState()}
               autofocus
               labelClassName="col-sm-2" wrapperClassName="col-sm-10"
               required />
      </div>
    );
  },
});

export default TimespanConfiguration;
