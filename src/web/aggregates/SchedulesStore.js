import Reflux from 'reflux';
import URLUtils from 'util/URLUtils';
import UserNotification from 'util/UserNotification';
import fetch from 'logic/rest/FetchProvider';

import SchedulesActions from './SchedulesActions';

const SchedulesStore = Reflux.createStore({
  listenables: [SchedulesActions],
  sourceUrl: '/plugins/org.graylog.plugins.aggregates/schedules',
  rules: undefined,
  
  init() {
	  this.trigger({ schedules: this.schedules});
  },

  list() {
    const promise = fetch('GET', URLUtils.qualifyUrl(this.sourceUrl))
      .then(
        response => {
          this.reportSchedules = response.report_schedules;
          this.trigger({ reportSchedules: this.reportSchedules });
          console.log('response: ' + JSON.stringify(response));
          return this.reportSchedules;
        },
        error => {
          UserNotification.error(`Fetching schedules failed with status: ${error}`,
            'Could not retrieve schedules');
        });
    SchedulesActions.list.promise(promise);
  },
  create(newSchedule) {
	const url = URLUtils.qualifyUrl(this.sourceUrl);
	const method = 'PUT';
	
	const request = {
			reportSchedule: {
				name: newSchedule.name,
				expression: newSchedule.expression
			}	
	};
    console.log('request: ' + JSON.stringify(request));
	const promise = fetch(method, url, request)
	  .then(() => {
	    UserNotification.success('Schedule successfully created');
	    this.list();
	  }, (error) => {
	    UserNotification.error(`Creating schedule failed with status: ${error.message}`,
	      'Could not create schedule');
	  });

	SchedulesActions.create.promise(promise);
	
  },
  update(name, updatedSchedule) {
		const url = URLUtils.qualifyUrl(this.sourceUrl+'/' + encodeURIComponent(name));
		const method = 'POST';
		
		const request = {
				reportSchedule: {
					name: updatedSchedule.name,
					interval: updatedSchedule.expression
				}
		};

		const promise = fetch(method, url, request)
		  .then(() => {
		    UserNotification.success('Schedule successfully updated');
		    this.list();
		  }, (error) => {
		    UserNotification.error(`Updating schedule failed with status: ${error.message}`,
		      'Could not update schedule');
		  });

		SchedulesActions.update.promise(promise);
		
	  },
  deleteByName(scheduleName) {
	const url = URLUtils.qualifyUrl(this.sourceUrl + '/' + encodeURIComponent(scheduleName));
	const method = 'DELETE';

	const promise = fetch(method, url)
	  .then(() => {
		UserNotification.success('Schedule successfully deleted');
		this.list();
	  }, (error) => {
		UserNotification.error(`Deleting schedule failed with status: ${error.message}`,
		  'Could not delete schedule');
	  });
	
    SchedulesActions.deleteByName.promise(promise);
  },
  
});

export default SchedulesStore;