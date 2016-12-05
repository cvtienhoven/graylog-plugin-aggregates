package org.graylog.plugins.aggregates.rule.alert;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailConstants;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.graylog.plugins.aggregates.rule.Rule;

import org.graylog2.alerts.StaticEmailAlertSender;
import org.graylog2.configuration.EmailConfiguration;
import org.graylog2.plugin.Tools;

import org.graylog2.plugin.alarms.transports.TransportConfigurationException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.indexer.searches.timeranges.TimeRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class RuleAlertSender {
	private final static String TABLE_STYLE = "style=\"font-family: 'sans-serif';font-size: 12px\"";
	private final static String TD_TR_STYLE = "style=\"padding: 3px;text-align:left;\"";
	
    protected final EmailConfiguration configuration;
    
    private Configuration pluginConfig;
    private static final Logger LOG = LoggerFactory.getLogger(StaticEmailAlertSender.class);
    
	@Inject
	public RuleAlertSender(EmailConfiguration configuration) {
		this.configuration = configuration;
	}

	
	public void initialize(Configuration configuration) {
		// TODO Auto-generated method stub
		
	}
	
	
	public void sendEmails(Rule rule, Map<String, Long> matchedTerms, TimeRange timeRange) throws EmailException, TransportConfigurationException{
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
        email.setMsg(buildBody(rule, matchedTerms, timeRange));
        
        for (String receiver : rule.getAlertReceivers()){
        	email.addTo(receiver);
        }
        LOG.info("sending alert to " + email.getToAddresses().toString());
        email.send();
	}

	private String buildBody(Rule rule, Map<String, Long> matchedTerms, TimeRange timeRange) {
        StringBuilder sb = new StringBuilder();
        String matchDescriptor = rule.getNumberOfMatches() + " or more";
        if (!rule.isMatchMoreOrEqual()){
        	matchDescriptor = "less than " + rule.getNumberOfMatches();
        }
        

        sb.append("<html>");
        sb.append("<body style=\"font-family: 'sans-serif';font-size: 12px\">");
        
        sb.append("<h1 style=\"font-size: 20px;\">" + rule.getName() + "</h1>");
        
        sb.append("<table " + TABLE_STYLE + ">");        
        sb.append("<tr " + TD_TR_STYLE + "><td " + TD_TR_STYLE + ">").append("<b>Date</b>: ").append("</td><td " + TD_TR_STYLE + ">").append(Tools.nowUTC().toString()).append("</td></tr>");
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
