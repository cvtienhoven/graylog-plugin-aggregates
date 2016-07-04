import React from 'react';
import { Button, Input, Alert, Row, Col } from 'react-bootstrap';

import BootstrapModalForm from 'components/bootstrap/BootstrapModalForm';
import ObjectUtils from 'util/ObjectUtils';

import ValidationsUtils from 'util/ValidationsUtils';

import AggregatesStore from './AggregatesStore';
import AggregatesActions from './AggregatesActions';


const EditRuleModal = React.createClass({
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
      },
    };
  },
  
  getInitialState() {
    return {
      originalName: ObjectUtils.clone(this.props.rule).name,
      rule: ObjectUtils.clone(this.props.rule),
      create: ObjectUtils.clone(this.props.create),
      rules: [],
    };
  },
  componentDidMount() {
    console.log("mount");    
  },
  openModal() {    
    this.refs.modal.open();
    this.setState(this.getInitialState());
    AggregatesActions.list().then(newRules => {
  	  this.setState({rules : newRules});      
    });    
  },

  _closeModal() {
    this.refs.modal.close();
  },

  _getId(prefixIdName) {
    return prefixIdName + this.state.rule.name;
  },

  _saved() {
    this._closeModal();
    this.setState(this.getInitialState());
  },

  _save() {
    const rule = this.state.rule;
    
    if (!rule.alertReceivers){
      rule.alertReceivers = [];
    }
     
    rule.alertReceivers = rule.alertReceivers.concat(this.state.emailReceiver);

	if (this.state.originalName != ''){
      this.props.createRule(this.state.originalName, rule, this._saved);
    } else {
      this.props.createRule(rule, this._saved);
    }

  },
  _onValueChanged(event) {
    
    const parameter = event.target.name;
    const value = event.target.value;
    
    console.log("onValueChanged: " + parameter + "=" + value);
    console.log("rules: " + this.state.rules);
    if (parameter == "name"){
    	const nameField = this.refs.name.getInputDOMNode();
    	const nameExists = this.state.rules.some(rule => rule.name === event.target.value);
    	ValidationsUtils.setFieldValidity(nameField, nameExists, 'Rule name is already taken');
    }        
    const rule = this.state.rule;
    rule[parameter] = value;
    
    this.setState(rule);
    
  },
  _onChangeEmail(evt) {
    this.setState({emailReceiver: evt.target.value});
  },  
  _formatReceiverList() {
    if (!this.state.rule.alertReceivers || this.state.rule.alertReceivers.length === 0) {
      return <Alert bsStyle="info">No configured alert receivers.</Alert>;
    }
    
    const emailReceivers = this.state.rule.alertReceivers.map((receiver) => {
      return (        
          <li>
            <i className="fa fa-envelope"/>{receiver}
          <a href="" onClick={this._onDelete}>
            <i className="fa fa-remove"/>
          </a>
        </li>);
    });
    return (
      <ul className="alert-receivers">
        {emailReceivers}
      </ul>
    );
  },
  render() {
    return (
      <span>
        <Button onClick={this.openModal}
                bsStyle={this.props.create ? 'success' : 'info'}
                bsSize={this.props.create ? null : 'xs'}>
          {this.props.create ? 'Create rule' : 'Edit'}
        </Button>
        <BootstrapModalForm ref="modal"
                            title={`${this.props.create ? 'Create' : 'Edit'} Rule ${this.state.originalName}`}
                            onSubmitForm={this._save}
                            submitButtonText="Save">
          <fieldset>
            
              <Input ref="name" name="name" id="name" type="text" maxLength={100}
              		labelClassName="col-sm-2" wrapperClassName="col-sm-10"
               		label="Name" help="Enter a unique rule name." required
               		onChange={this._onValueChanged} autoFocus  />
            
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
               		
			  <Row id="add-alert-receivers" className="row-sm">          
            	<Col md={6}>
				  <ul className="alert-receivers">
	         		{this._formatReceiverList()}
          		  </ul>
				</Col>
          	  </Row>              
              
			  <Input ref="email" label="Email address:" type="text" value={this.state.emailReceiver} onChange={this._onChangeEmail}/>				  			  
			  
          </fieldset>

        </BootstrapModalForm>
        
        
      </span>
    );
  },
});

export default EditRuleModal;