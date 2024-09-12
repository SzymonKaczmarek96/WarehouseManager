package Warehouse.WarehouseManager.email;

import Warehouse.WarehouseManager.employee.EmployeeDto;
import Warehouse.WarehouseManager.security.SecurityService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SecurityService securityService;

    public EmailService(final JavaMailSender mailSender, final SecurityService securityService) {
        this.mailSender = mailSender;
        this.securityService = securityService;
    }

    public void sendActivationEmail(EmployeeDto employeeDto) {
        String token = securityService.generateActivationToken(employeeDto);
        String URL = "http://localhost:8080/api/employee/activate/" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(employeeDto.email());
        message.setSubject("Hi " + employeeDto.username() + "! Activate your account.");
        message.setText("Please click on the following link do activate your account:\n" + URL);

        mailSender.send(message);
    }

}
