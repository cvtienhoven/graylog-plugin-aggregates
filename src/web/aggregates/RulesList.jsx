import React from 'react';
import Reflux from 'reflux';
import { LinkContainer } from 'react-router-bootstrap';
import { Button } from 'react-bootstrap';

import PermissionsMixin from 'util/PermissionsMixin';
import AggregatesStore from './AggregatesStore';
import AggregatesActions from './AggregatesActions';
import SchedulesStore from './SchedulesStore';
import SchedulesActions from './SchedulesActions';
import EditRuleModal from './EditRuleModal';

import StoreProvider from 'injection/StoreProvider';
const StreamsStore = StoreProvider.getStore('Streams');

import { DataTable, Spinner, IfPermitted } from 'components/common';

const RulesList = React.createClass({
  mixins: [Reflux.connect(AggregatesStore)],
    
  getInitialState() {
    return {
      rules: undefined,
      streams: [],
    };
  },
  _editRule(originalName, rule, callback) {
    AggregatesActions.update.triggerPromise(originalName, rule)
      .then(() => {
        callback();
      });
  },
  componentDidMount() {
    this.list();
  },    
  list() {
	StreamsStore.listStreams().then(list => {
      this.setState({streams: list});
    });  	
    AggregatesActions.list().then(newRules => {
  	  this.setState({rules : newRules});      
    });
    SchedulesActions.list().then(newSchedules => {
      this.setState({reportSchedules: newSchedules});
    });
  },
  deleteRule(name) {
    AggregatesActions.deleteByName(name);
  },
  toggleEnabled(rule) {
    rule.enabled = !rule.enabled;
    AggregatesActions.update(rule.name, rule);
  },
  toggleInReport(rule) {
    rule.inReport = !rule.inReport;
    AggregatesActions.update(rule.name, rule);
  },
  _deleteRuleFunction(name) {
    return () => {
      if (window.confirm('Do you really want to delete rule ' + name + '?')) {
        this.deleteRule(name);
      }
    };
  },
  _toggleRuleFunction(rule) {
    return () => {
      let text = rule.enabled === true ? 'disable' : 'enable'
      if (window.confirm('Do you really want to ' + text + ' rule ' + rule.name + '?')) {
        this.toggleEnabled(rule);
      }
    };
  },
  _toggleRuleInReportFunction(rule) {
    return () => {
      
        this.toggleInReport(rule);
      
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
  _alertReceiversFormatter(rule){
    const emailReceivers = rule.alertReceivers.map((receiver) => {
      return (        
          <li key={receiver}>
            <i className="fa fa-envelope"/> {receiver}
            
        </li>);
    
     });
   return (
     <ul className="alert-receivers">
       {emailReceivers}
     </ul>
    );
  },
  _reportScheduleFormatter(rule){
    var reportSchedules = '';
    if (rule.reportSchedules != null && this.state.reportSchedules != null) {
      reportSchedules = rule.reportSchedules === null ? '' : rule.reportSchedules.map((reportSchedule) => {
        for (var i=0; i<this.state.reportSchedules.length; i++){
          if (reportSchedule == this.state.reportSchedules[i]._id){
            name = this.state.reportSchedules[i].name;
            break;
          }
        }
                
        return (        
          <li key={reportSchedule}>
            <i className="fa fa-clock-o"/> {name}
           
          </li>);
    
       });
   }
   return (
     <ul className="alert-receivers">
       {reportSchedules}
     </ul>
    );
  },     
  _ruleInfoFormatter(rule) {
 	const match = (
 		rule.matchMoreOrEqual === true ? rule.numberOfMatches + ' or more' : 'less than ' + rule.numberOfMatches 	
 	);
 	
 	
 	const inReport = (
 		<input id="toggle-in-report" type="checkbox" checked={rule.inReport} onClick={this._toggleRuleInReportFunction(rule)} ></input>
 	);
 	
    const deleteAction = (
      <IfPermitted permissions="aggregate_rules:delete">          	           	
        <button id="delete-rule" type="button" className="btn btn-xs btn-primary" title="Delete rule"
              onClick={this._deleteRuleFunction(rule.name)}>
          Delete
        </button>
      </IfPermitted>
    );

    const toggleText = (
 	  rule.enabled === true ? 'Disable' : 'Enable'
 	);

    const toggleAction = (
      <IfPermitted permissions="aggregate_rules:update">
        <button id="toggle-rule" type="button" className="btn btn-xs btn-primary" title="{toggleText}"
              onClick={this._toggleRuleFunction(rule)}>
          {toggleText}
        </button>
      </IfPermitted>
    );

    const editAction = (
      <IfPermitted permissions="aggregate_rules:update">
        <EditRuleModal create={false} createRule={this._editRule} rule={rule}/>
      </IfPermitted>
    );

	  

    const actions = (
      <div>
        {deleteAction}
        &nbsp;
        {editAction}
        &nbsp;
        {toggleAction}
      </div>
    );
    
    var streamTitle = "--No Stream (global search)--";
    if (rule.streamId != '') {
      for (var i=0; i<this.state.streams.length; i++){
        if (this.state.streams[i].id == rule.streamId) {
          streamTitle = this.state.streams[i].title;
        }
      }
     }
    
    
    return (
      <tr key={rule.name}>
        <td className="limited">{rule.name}</td>
        <td className="limited">{rule.query}</td>
        <td className="limited">The same value of field '{rule.field}' occurs {match} times in a {rule.interval} minute interval</td>
		<td className="limited">{this._alertReceiversFormatter(rule)}</td>
		<td className="limited">{streamTitle}</td>
		<td>{inReport}</td>
		<td>{this._reportScheduleFormatter(rule)}</td>
        <td>{actions}</td>
      </tr>
    );
  },
  render() {
    const filterKeys = ['name', 'query', 'field', 'stream'];
    const headers = ['Rule name', 'Query', 'Alert condition', 'Alert receivers', 'Stream', 'In report', 'Schedules'];
    
    if (this.state.rules) {
      return (
        <div>
          <DataTable id="rule-list"
                     className="table-hover"
                     headers={headers}
                     headerCellFormatter={this._headerCellFormatter}
                     sortByKey={"name"}
                     rows={this.state.rules}
                     filterBy="field"                     
                     dataRowFormatter={this._ruleInfoFormatter}
                     filterLabel="Filter Rules"
                     filterKeys={filterKeys}/>
        </div>
      );
    }

    return <Spinner />;
  },
});

export default RulesList;
