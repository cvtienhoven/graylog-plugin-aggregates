import React from 'react';
import Reflux from 'reflux'
import { Row, Col, Button } from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';

import DocsHelper from 'util/DocsHelper';

import SchedulesActions from './SchedulesActions';
import SchedulesList from './SchedulesList';
import EditScheduleModal from './EditScheduleModal';
import DocumentationLink from 'components/support/DocumentationLink';
import { IfPermitted, PageHeader } from 'components/common';


const SchedulesPage = React.createClass({
  mixins: [],
  _createReportSchedule(reportSchedule, callback) {
    SchedulesActions.create.triggerPromise(reportSchedule)
      .then(() => {
        callback();
      });
  },  
  render() {
    return (
      <span>
        <PageHeader title="Aggregate Reporting Schedules">
          <span>
            Define your reporting schedules here.
          </span>

          <span>
            <IfPermitted permissions="aggregate_rules:create">
          	  <EditScheduleModal create createReportSchedule={this._createReportSchedule}/>
          	</IfPermitted>
          </span>
        </PageHeader>

        <Row className="content">
          <Col md={12}>
            <SchedulesList />
          </Col>
        </Row>
      </span>
    );
  },
});

export default SchedulesPage;