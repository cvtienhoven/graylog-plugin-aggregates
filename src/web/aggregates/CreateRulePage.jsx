import React from 'react';
import Reflux from 'reflux'
import { Row, Col, Input, Button } from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';

import DocsHelper from 'util/DocsHelper';

import DocumentationLink from 'components/support/DocumentationLink';
import { IfPermitted, PageHeader } from 'components/common';

import ValidationsUtils from 'util/ValidationsUtils';

import AggregatesStore from './AggregatesStore';
import AggregatesActions from './AggregatesActions';


const AddRulePage = React.createClass({
  mixins: [],
  _onNameChange(event) {
    const nameField = this.refs.name.getInputDOMNode();
    const nameExists = this.state.rules.some(rule => rule.name === event.target.value);

    ValidationsUtils.setFieldValidity(nameField, nameExists, 'Rule name is already taken');
  },
  propTypes: {    
    create: React.PropTypes.bool, 
  },
  getInitialState() {  	
    return {
      create: true,
      rules: [],
    };
  },
  componentDidMount() {
    AggregatesActions.list().then(rules => {
      this.setState({ rules, create: this.state.create });
    });
  },
  _onSubmit(evt) {
    evt.preventDefault();
    const result = {};
    Object.keys(this.refs).forEach((ref) => {      
      result[ref] = (this.refs[ref].getValue ? this.refs[ref].getValue() : this.refs[ref].value);
    });
	
	AggregatesActions.create(result);
	this.setState({ rules: this.state.rules, create: false });

  },
  render() {
    return (
      <span>
        <PageHeader title="Add Rule">
          <span>
            Add a rule. 
          </span>

          <span>
            
          </span>
        </PageHeader>

        <Row className="content">
          <Col md={12}>            
            <form id="create-rule-form" className="form-horizontal" onSubmit={this._onSubmit}>
                          
        	  <Input ref="name" name="name" id="name" type="text" maxLength={100}
               		labelClassName="col-sm-2" wrapperClassName="col-sm-10"
               		label="Name" help="Enter a unique rule name." required
               		onChange={this._onNameChange} autoFocus disabled="{!this.props.create}" />


        	  <Input ref="query" name="query" id="query" type="text" maxLength={400}
               		labelClassName="col-sm-2" wrapperClassName="col-sm-10"
               		label="Query" help="Execute this query..." required />


		      <Input ref="field" name="field" id="field" type="text" maxLength={50}
               		labelClassName="col-sm-2" wrapperClassName="col-sm-10"
               		label="Field" help="...and check if a value of the above field occurs..." required />

              <Input ref="matchMoreOrEqual" name="matchMoreOrEqual" id="matchMoreOrEqual" type="select" defaultValue="true"
                    labelClassName="col-sm-2" wrapperClassName="col-sm-10"
               	    label="Match more or equal" help="...than... " required >
               	    	<option value="true">More or equal</option>
          				<option value="false">Less</option>
			  </Input>        
			  
			  			  
			  <Input ref="numberOfMatches" name="numberOfMatches" id="numberOfMatches" type="number" maxLength={5}
               		labelClassName="col-sm-2" wrapperClassName="col-sm-10"
               		label="#Matches" help="...times in a..." required />
              		
              <Input ref="interval" name="interval" id="interval" type="number" maxLength={5}
               		labelClassName="col-sm-2" wrapperClassName="col-sm-10"
               		label="Interval (minutes)" help="...minute interval." required />
               		               	        		
              <div className="form-group">
                <Col smOffset={2} sm={10}>
                  <Button type="submit" bsStyle="success">
                    Create Rule
                  </Button>
          		</Col>
        	  </div>
      		</form>
          </Col>
        </Row>
      </span>
    );
  },
});

export default AddRulePage;