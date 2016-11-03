package org.graylog.plugins.aggregates.report;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
 
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailConstants;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.graylog2.configuration.EmailConfiguration;
import org.graylog2.plugin.alarms.transports.TransportConfigurationException;
import org.graylog2.plugin.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
 

public class ReportSender {
     
protected final EmailConfiguration configuration;
    
    private Configuration pluginConfig;
    private static final Logger LOG = LoggerFactory.getLogger(ReportSender.class);
    
	@Inject
	public ReportSender(EmailConfiguration configuration) {
		this.configuration = configuration;
	}
	
    /**
     * Sends an email with a PDF attachment.
     * @throws TransportConfigurationException 
     * @throws EmailException 
     * @throws MessagingException 
     */
    public void sendEmail(String receipient, byte[] pdf, String description) throws TransportConfigurationException, EmailException, MessagingException {
    	if(!configuration.isEnabled()) {
            throw new TransportConfigurationException("Email transport is not enabled in server configuration file!");
        }

        final MultiPartEmail email = new MultiPartEmail();
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
        
        
        email.setSubject("Graylog " + description + " Aggregates Report");

        Calendar c = Calendar.getInstance();
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
        email.attach(new ByteArrayDataSource(pdf, "application/pdf"),
        	      description.toLowerCase() + "_aggregates_report_" + df.format(c.getTime()) +".pdf", "Graylog " + description + "Aggregates Report",
        	       EmailAttachment.ATTACHMENT);
                
        
        email.setMsg("Please find the report attached.");

        email.addTo(receipient);
                	
       	LOG.info("sending report to " + email.getToAddresses().toString());
        	
       	email.send();
        
    }
   
}