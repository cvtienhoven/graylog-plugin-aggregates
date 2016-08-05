import React from 'react';
import Reflux from 'reflux';
import { LinkContainer } from 'react-router-bootstrap';
import { Button } from 'react-bootstrap';

import PermissionsMixin from 'util/PermissionsMixin';
import AggregatesStore from './AggregatesStore';
import AggregatesActions from './AggregatesActions';
import EditRuleModal from './EditRuleModal';

import StoreProvider from 'injection/StoreProvider';
const StreamsStore = StoreProvider.getStore('Streams');

import DataTable from 'components/common/DataTable';
import Spinner from 'components/common/Spinner';

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
  },
  deleteRule(name) {
    AggregatesActions.deleteByName(name);
  },
  toggleEnabled(rule) {
    rule.enabled = !rule.enabled;
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
            <i className="fa fa-envelope"/>{receiver}
            
        </li>);
    
     });
   return (
     <ul className="alert-receivers">
       {emailReceivers}
     </ul>
    );
  },     
  _ruleInfoFormatter(rule) {
 	const match = (
 		rule.matchMoreOrEqual === true ? rule.numberOfMatches + ' or more' : 'less than ' + rule.numberOfMatches 	
 	);
 	
 	
 	
    const deleteAction = (
      <button id="delete-rule" type="button" className="btn btn-xs btn-primary" title="Delete rule"
              onClick={this._deleteRuleFunction(rule.name)}>
        Delete
      </button>
    );

    const toggleText = (
 	  rule.enabled === true ? 'Disable' : 'Enable'
 	)

    const toggleAction = (
      <button id="toggle-rule" type="button" className="btn btn-xs btn-primary" title="{toggleText}"
              onClick={this._toggleRuleFunction(rule)}>
        {toggleText}
      </button>
    );

    const editAction = (
      <EditRuleModal create={false} createRule={this._editRule} rule={rule}/>
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
    
    var streamTitle = "";
    for (var i=0; i<this.state.streams.length; i++){
      if (this.state.streams[i].id == rule.streamId) {
        streamTitle = this.state.streams[i].title;
      }
    }
    
    
    return (
      <tr key={rule.name}>
        <td className="limited">{rule.name}</td>
        <td className="limited">{rule.query}</td>
        <td className="limited">The same value of field '{rule.field}' occurs {match} times in a {rule.interval} minute interval</td>
		<td className="limited">{this._alertReceiversFormatter(rule)}</td>
		<td className="limited">{streamTitle}</td>
        <td>{actions}</td>
      </tr>
    );
  },
  render() {
    const filterKeys = ['name', 'query', 'field', 'stream'];
    const headers = ['Rule name', 'Query', 'Alert condition', 'Alert receivers', 'Stream'];
    
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
