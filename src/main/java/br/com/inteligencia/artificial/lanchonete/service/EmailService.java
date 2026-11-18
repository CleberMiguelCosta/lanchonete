package br.com.inteligencia.artificial.lanchonete.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final ObjectProvider<JavaMailSender> mailSender;
	private final boolean mailEnabled;

	public EmailService(ObjectProvider<JavaMailSender> mailSender, @Value("${app.mail.enabled:false}") boolean mailEnabled) {
		this.mailSender = mailSender;
		this.mailEnabled = mailEnabled;
	}

	public void enviar(String destinatario, String assunto, String corpo) {
		log.info("E-mail para {}: {}\n{}", destinatario, assunto, corpo);
		if (!mailEnabled) {
			return;
		}
		JavaMailSender sender = mailSender.getIfAvailable();
		if (sender == null) {
			log.warn("JavaMailSender indisponível; e-mail apenas registrado no log.");
			return;
		}
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(destinatario);
			message.setSubject(assunto);
			message.setText(corpo);
			sender.send(message);
		} catch (Exception ex) {
			log.warn("Falha ao enviar e-mail real, conteúdo mantido no log: {}", ex.getMessage());
		}
	}
}
