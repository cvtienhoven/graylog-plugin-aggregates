package org.graylog.plugins.aggregates.rule.alert;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailConstants;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.graylog.plugins.aggregates.rule.Rule;
import org.graylog2.alerts.AlertSender;
import org.graylog2.alerts.MessageFormatter;
import org.graylog2.alerts.StaticEmailAlertSender;
import org.graylog2.configuration.EmailConfiguration;
import org.graylog2.notifications.NotificationService;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.Tools;
import org.graylog2.plugin.alarms.AlertCondition.CheckResult;
import org.graylog2.plugin.alarms.transports.TransportConfigurationException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugin.system.NodeId;
import org.graylog2.shared.users.UserService;
import org.graylog2.streams.StreamRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class RuleAlertSender {
    protected final EmailConfiguration configuration;
    private final UserService userService;
    private final NodeId nodeId;
    private Configuration pluginConfig;
    private static final Logger LOG = LoggerFactory.getLogger(StaticEmailAlertSender.class);
    
	@Inject
	public RuleAlertSender(EmailConfiguration configuration, UserService userService,
			NodeId nodeId) {
		this.configuration = configuration;
        this.userService = userService;
        this.nodeId = nodeId;
		// TODO Auto-generated constructor stub
	}

	
	public void initialize(Configuration configuration) {
		// TODO Auto-generated method stub
		
	}
	
	
	public void sendEmails(String emailAddress, Rule rule, Map<String, Long> matchedTerms) throws EmailException, TransportConfigurationException{
		LOG.debug("Sending mail to " + emailAddress);
        if(!configuration.isEnabled()) {
            throw new TransportConfigurationException("Email transport is not enabled in server configuration file!");
        }

        final Email email = new SimpleEmail();
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
        
        
        email.setSubject("Rule [" + rule.getName() + "] triggered and alert");
        email.setMsg(buildBody(rule, matchedTerms));
        
        email.addTo(emailAddress);

        email.send();
	}

	private String buildBody(Rule rule, Map<String, Long> matchedTerms) {
        StringBuilder sb = new StringBuilder();
        String matchDescriptor = " at least ";
        if (!rule.isMatchMoreOrEqual()){
        	matchDescriptor = " less than";
        }
        
        //sb.append(checkResult.getResultDescription());

        sb.append("\n\n");
        sb.append("##########\n");
        sb.append("Date: ").append(Tools.nowUTC().toString()).append("\n");
        sb.append("Query: ").append(rule.getQuery()).append("\n");        
        sb.append("Condition: ").append(rule.getField() + " matched " + matchDescriptor).append(rule.getNumberOfMatches() + " times in the last " + rule.getInterval() + " minutes").append("\n\n");                
        sb.append("##########\n\n");
        
        sb.append(buildSummary(rule, matchedTerms));
        

        return sb.toString();
    }
	
	protected String buildSummary(Rule rule, Map<String, Long> matchedTerms) {
        

        final StringBuilder sb = new StringBuilder();
        
        sb.append("\nMatched values for [" + rule.getField() + "]\n\n");
        
        for (Map.Entry<String, Long> entry: matchedTerms.entrySet() ) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue());
            sb.append("\n");
        }

        return sb.toString();
    }
	
}
