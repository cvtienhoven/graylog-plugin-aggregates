import React from 'react';
import { Button, Input, Alert, Row, Col } from 'react-bootstrap';

import BootstrapModalForm from 'components/bootstrap/BootstrapModalForm';
import ObjectUtils from 'util/ObjectUtils';

import ValidationsUtils from 'util/ValidationsUtils';

import SchedulesStore from './SchedulesStore';
import SchedulesActions from './SchedulesActions';

import { IfPermitted } from 'components/common';

import StoreProvider from 'injection/StoreProvider';
const StreamsStore = StoreProvider.getStore('Streams');

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
      	expression: '* * * * * *'
      },
    };
  },
  
  getInitialState() {
    return {
      originalName: ObjectUtils.clone(this.props.reportSchedule).name,
      reportSchedule: ObjectUtils.clone(this.props.reportSchedule),
      create: ObjectUtils.clone(this.props.create),
      reportSchedules: []
    };
  },
  
  
  componentDidMount() {
       
  },
  openModal() {    
    this.refs.modal.open();
    this.setState(this.getInitialState());
  },

  _closeModal() {
    this.refs.modal.close();
  },

  _getId(prefixIdName) {
    return prefixIdName + this.state.reportSchedule.name;
  },

  _saved() {
    this._closeModal();
    this.setState(this.getInitialState());
  },

  _save() {
    const reportSchedule = this.state.reportSchedule;
    

	if (this.state.originalName != ''){
      this.props.createReportSchedule(this.state.originalName, reportSchedule, this._saved);
    } else {
      this.props.createReportSchedule(reportSchedule, this._saved);
    }

  },   
  _onValueChanged(event) {
    
    const reportSchedule = this.state.reportSchedule;
    
    const parameter = event.target.name;
    const value = event.target.value;
    
    if (parameter == 'name'){
	  reportSchedule.name = value;
	  console.log('name changed')
    }
    
    if (parameter == "expression"){
      reportSchedule.expression = value;
      console.log('expression changed')
    }
    

    this.setState(reportSchedule);
    console.log('state: ' + JSON.stringify(this.state))
    
  },  
  render() {
    return (
      <span>
        <Button onClick={this.openModal}
                bsStyle={this.props.create ? 'success' : 'info'}
                bsSize={this.props.create ? null : 'xs'}>
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
               		onChange={this._onValueChanged} autoFocus  />
             
            
              <Input ref="expression" name="expression" id="expression" type="text" maxLength={100} defaultValue='* * * * *'
              		labelClassName="col-sm-2" wrapperClassName="col-sm-10"
               		label="Cron Expression" help="Enter a cron expression" required
               		onChange={this._onValueChanged} autoFocus  />

          </fieldset>

        </BootstrapModalForm>
        
        
      </span>
    );
  },
});

export default EditScheduleModal;