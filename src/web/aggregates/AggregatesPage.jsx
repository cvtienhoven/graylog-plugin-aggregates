import React from 'react';
import { Row, Col, Button } from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';
import AggregatesActions from './AggregatesActions';
import RulesList from './RulesList';
import EditRuleModal from './EditRuleModal';
import { IfPermitted, PageHeader } from 'components/common';

const AggregatesPage = React.createClass({
  mixins: [],
  _createRule(rule, callback) {
    AggregatesActions.create.triggerPromise(rule)
      .then(() => {
        callback();
      });
  },
  render() {
    return (
      <span>
        <PageHeader title="Aggregate Rules">
          <span>
            With aggregate rules, you can define a rule with criteria for a terms search, so you can generate an alert when the same value for field X occurs Y times in a Z minute time frame.
          </span>

          <span>
            <IfPermitted permissions="aggregate_rules:create">
              <EditRuleModal create createRule={this._createRule}/>
            </IfPermitted>
          </span>
          <span>
            <IfPermitted permissions="aggregate_report_schedules:read">
              <LinkContainer to="/aggregates/schedules">
                <Button bsStyle="info">Manage Report Schedules</Button>
              </LinkContainer>
            </IfPermitted>
          </span>
        </PageHeader>

        
        <Row className="content">
          <Col md={12}>
            <IfPermitted permissions={['aggregate_rules:read', 'aggregate_report_schedules:read']}>
              <RulesList />
            </IfPermitted>
          </Col>
        </Row>
      </span>
    );
  },
});

export default AggregatesPage;
