import React from 'react';
import Reflux from 'reflux';
import { LinkContainer } from 'react-router-bootstrap';
import { Button } from 'react-bootstrap';

import PermissionsMixin from 'util/PermissionsMixin';
import AggregatesStore from './AggregatesStore';
import AggregatesActions from './AggregatesActions';
import EditRuleModal from './EditRuleModal';

import DataTable from 'components/common/DataTable';
import Spinner from 'components/common/Spinner';

const RulesList = React.createClass({
  mixins: [Reflux.connect(AggregatesStore)],
    
  getInitialState() {
    return {
      rules: undefined,
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
    AggregatesActions.list().then(newRules => {
  	  this.setState({rules : newRules});      
    });  
  },
  deleteRule(name) {
    AggregatesActions.deleteByName(name);
  },
  _deleteRuleFunction(name) {
    return () => {
      if (window.confirm('Do you really want to delete rule ' + name + '?')) {
        this.deleteRule(name);
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

      const editAction = (
        <EditRuleModal create={false} createRule={this._editRule} rule={rule}/>         
      );

      const actions = (
        <div>
          {deleteAction}
          &nbsp;
          {editAction}
        </div>
      );
    
    
    return (
      <tr key={rule.name}>
        <td className="limited">{rule.name}</td>
        <td className="limited">{rule.query}</td>
        <td className="limited">{rule.field}</td>
        <td className="limited">{match}</td>
		<td className="limited">{rule.interval} minutes</td>		
        <td>{actions}</td>
      </tr>
    );
  },
  render() {
    const filterKeys = ['name', 'query', 'field'];
    const headers = ['Name', 'Query', 'Field', '# Matches', 'Interval'];
    
    if (this.state.rules) {
      return (
        <div>
          <DataTable id="rule-list"
                     className="table-hover"
                     headers={headers}
                     headerCellFormatter={this._headerCellFormatter}
                     sortByKey={"field"}
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
