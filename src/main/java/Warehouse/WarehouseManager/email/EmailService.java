package Warehouse.WarehouseManager.email;

import Warehouse.WarehouseManager.employee.EmployeeDto;
import Warehouse.WarehouseManager.security.SecurityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SecurityService securityService;
    private final String serverURL;

    public EmailService(final JavaMailSender mailSender, final SecurityService securityService, @Value("${server.url}") String serverURL) {
        this.mailSender = mailSender;
        this.securityService = securityService;
        this.serverURL = serverURL;
    }

    public void sendActivationEmail(EmployeeDto employeeDto) {
        String activationToken = securityService.generateActivationToken(employeeDto);
        String URL = serverURL + "/api/employee/activate/" + activationToken;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(employeeDto.email());
        message.setSubject("Hi " + employeeDto.username() + "! Activate your account.");
        message.setText("Please click on the following link to activate your account:\n" + URL);

        mailSender.send(message);
    }
}
