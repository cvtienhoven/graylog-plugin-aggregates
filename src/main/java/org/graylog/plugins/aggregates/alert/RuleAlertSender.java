package org.graylog.plugins.aggregates.alert;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailConstants;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog.plugins.aggregates.util.AggregatesUtil;
import org.graylog2.alarmcallbacks.AlarmCallbackConfiguration;
import org.graylog2.alarmcallbacks.AlarmCallbackConfigurationService;
import org.graylog2.alarmcallbacks.AlarmCallbackFactory;
import org.graylog2.alarmcallbacks.EmailAlarmCallback;
import org.graylog2.configuration.EmailConfiguration;
import org.graylog2.database.NotFoundException;
import org.graylog2.plugin.Tools;
import org.graylog2.plugin.alarms.callbacks.AlarmCallback;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.alarms.transports.TransportConfigurationException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.indexer.searches.timeranges.TimeRange;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.streams.StreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

public class RuleAlertSender {
	private final static String TABLE_STYLE = "style=\"font-family: 'sans-serif';font-size: 12px\"";
	private final static String TD_TR_STYLE = "style=\"padding: 3px;text-align:left;\"";
	
    protected final EmailConfiguration configuration;
    protected final AlarmCallbackConfigurationService alarmCallbackConfigurationService;
    protected final AlarmCallbackFactory alarmCallbackFactory;
    protected final StreamService streamService;
    private Configuration pluginConfig;
    private static final Logger LOG = LoggerFactory.getLogger(RuleAlertSender.class);
    private AggregatesUtil aggregatesUtil;
    
	@Inject
	public RuleAlertSender(EmailConfiguration configuration, AlarmCallbackConfigurationService alarmCallbackConfigurationService, AlarmCallbackFactory alarmCallbackFactory, StreamService streamService
    ) {
		this.configuration = configuration;
		this.alarmCallbackConfigurationService = alarmCallbackConfigurationService;
		this.alarmCallbackFactory = alarmCallbackFactory;
		this.streamService = streamService;
		setAggregatesUtil(new AggregatesUtil());
	}

	void setAggregatesUtil(AggregatesUtil aggregatesUtil){
		this.aggregatesUtil = aggregatesUtil;
	}
	
	public void initialize(Configuration configuration) {
		// TODO Auto-generated method stub
		
	}

	public void send(Rule rule, Map<String, Long> matchedTerms, TimeRange timeRange) throws NotFoundException, AlarmCallbackConfigurationException, ClassNotFoundException, AlarmCallbackException, UnsupportedEncodingException {
        AlarmCallbackConfiguration alarmCallbackConfiguration = alarmCallbackConfigurationService.load(rule.getNotificationId());
        
        AlarmCallback callback = alarmCallbackFactory.create(alarmCallbackConfiguration);
        
        Stream triggeredStream = streamService.load(rule.getStreamId());
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("time", rule.getInterval());
        String title = "Aggregate rule [" + rule.getName() + "] triggered an alert.";
        
        String description = aggregatesUtil.buildSummary(rule, configuration, matchedTerms, timeRange);
                                        
        AggregatesAlertCondition alertCondition = new AggregatesAlertCondition(rule, description, triggeredStream, "", "", timeRange.getFrom(), "", new HashMap<String, Object>(), title);
        
        LOG.info("callback to be invoked: " + callback.getName());
                
        callback.call(streamService.load(rule.getStreamId()), alertCondition.runCheck());

    }

	
	public void sendEmails(Rule rule, Map<String, Long> matchedTerms, TimeRange timeRange, Date date) throws EmailException, TransportConfigurationException{
        if(!configuration.isEnabled()) {
            throw new TransportConfigurationException("Email transport is not enabled in server configuration file!");
        }

        final Email email = new HtmlEmail();
        email.setCharset(EmailConstants.UTF_8);
        

        if (Strings.isNullOrEmpty(configuration.getHostname())) {
            throw new TransportConfigurationException("No hostname configured for email transport while trying to send alert email!");
        } else {
            email.setHostName(configuration.getHostname());
        }
        email.setSmtpPort(configuration.getPort());
        if (configuration.isUseSsl()) {
            email.setSslSmtpPort(Integer.toString(configuration.getPort()));
        }

        if(configuration.isUseAuth()) {
            email.setAuthenticator(new DefaultAuthenticator(
                    Strings.nullToEmpty(configuration.getUsername()),
                    Strings.nullToEmpty(configuration.getPassword())
            ));
        }

        email.setSSLOnConnect(configuration.isUseSsl());
        email.setStartTLSEnabled(configuration.isUseTls());
        if (pluginConfig != null && !Strings.isNullOrEmpty(pluginConfig.getString("sender"))) {
            email.setFrom(pluginConfig.getString("sender"));
        } else {
            email.setFrom(configuration.getFromEmail());
        }
        
        
        email.setSubject("Aggregate Rule [ " + rule.getName() + " ] triggered an alert");
        email.setMsg(buildBody(rule, matchedTerms, timeRange, date));
        
        //for (String receiver : rule.getAlertReceivers()){
        //	email.addTo(receiver);
        //}
        LOG.info("sending alert to " + email.getToAddresses().toString());
        email.send();
	}

	private String buildBody(Rule rule, Map<String, Long> matchedTerms, TimeRange timeRange, Date date) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        String matchDescriptor = rule.getNumberOfMatches() + " or more";
        if (!rule.isMatchMoreOrEqual()){
        	matchDescriptor = "less than " + rule.getNumberOfMatches();
        }
        

        sb.append("<html>");
        sb.append("<body style=\"font-family: 'sans-serif';font-size: 12px\">");
        
        sb.append("<h1 style=\"font-size: 20px;\">" + rule.getName() + "</h1>");
        
        sb.append("<table " + TABLE_STYLE + ">");        
        sb.append("<tr " + TD_TR_STYLE + "><td " + TD_TR_STYLE + ">").append("<b>Date</b>: ").append("</td><td " + TD_TR_STYLE + ">").append(df.format(date)).append("</td></tr>");
        sb.append("<tr " + TD_TR_STYLE + "><td " + TD_TR_STYLE + ">").append("<b>Query</b>: ").append("</td><td " + TD_TR_STYLE + ">").append(rule.getQuery()).append("</td></tr>");                
        sb.append("<tr " + TD_TR_STYLE + "><td " + TD_TR_STYLE + ">").append("<b>Alert condition</b>: ").append("</td><td " + TD_TR_STYLE + ">").append("The same value of field '" + rule.getField() + "' occurs " + matchDescriptor + " times in a " + rule.getInterval() + " minute interval").append("</td></tr>");                
        sb.append("</table>");
        
        sb.append(buildSummary(rule, matchedTerms, timeRange));
        
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }
	
	protected String buildSummary(Rule rule, Map<String, Long> matchedTerms, TimeRange timeRange) {
        

        final StringBuilder sb = new StringBuilder();
        
        
        
        sb.append("<h2 style=\"font-size: 15px;\">Matched values for field [ " + rule.getField() + " ]</h2>");
        
        sb.append("<table border=\"1\"" + TABLE_STYLE + "><tr " + TD_TR_STYLE + "><th " + TD_TR_STYLE + ">Value</th><th " + TD_TR_STYLE + ">#Occurrences</th><th " + TD_TR_STYLE + "></th></tr>");
        
        for (Map.Entry<String, Long> entry: matchedTerms.entrySet() ) {
        	sb.append("<tr " + TD_TR_STYLE + ">");
        	sb.append("<td " + TD_TR_STYLE + ">").append(entry.getKey()).append("</td>");
        	sb.append("<td " + TD_TR_STYLE + ">").append(entry.getValue()).append("</td>");
        	        	        	
        	try {
        		String streamId = rule.getStreamId();
        		String search_uri = "";
        		
        		if (streamId != null && streamId != ""){
        			search_uri+="/streams/"+streamId;
        		}
        		search_uri+="/search?rangetype=absolute&fields=message%2Csource%2C"+rule.getField()+"&from="+timeRange.getFrom()+"&to="+timeRange.getTo()+"&q="+URLEncoder.encode(rule.getQuery()+" AND " + rule.getField()+":\""+entry.getKey()+"\"", "UTF-8");
				sb.append("<td " + TD_TR_STYLE + ">").append("<a href=\""+configuration.getWebInterfaceUri()+search_uri+"\">Search</a>").append("</td>");
			} catch (UnsupportedEncodingException e) {
				sb.append("<td " + TD_TR_STYLE + ">").append("Unable to URL encode search URI").append("</td>");
			}
            
            sb.append("</tr>");
        }        
        sb.append("</table>");
        return sb.toString();
    }
	
}
