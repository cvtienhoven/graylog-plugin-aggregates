import React from 'react';
import Reflux from 'reflux'
import { Row, Col, Button } from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';

import DocsHelper from 'util/DocsHelper';

import AggregatesActions from './AggregatesActions'
import RulesList from './RulesList';
import EditRuleModal from './EditRuleModal';
import DocumentationLink from 'components/support/DocumentationLink';
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
        <PageHeader title="Aggregate Search Rules">
          <span>
            With aggregate search rules, you can define a rule with search criteria and unique value counts for a specified field within a certain time range.  
          </span>

          <span>
          	<EditRuleModal create createRule={this._createRule}/>            
          </span>
        </PageHeader>

        <Row className="content">
          <Col md={12}>
            <RulesList />
          </Col>
        </Row>
      </span>
    );
  },
});

export default AggregatesPage;