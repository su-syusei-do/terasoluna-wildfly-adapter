package com.example.app.welcome;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.*;
import java.security.Principal;
import org.wildfly.security.http.oidc.*;
import org.apache.commons.lang3.builder.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HelloController {

    private static final Logger logger = LoggerFactory
            .getLogger(HelloController.class);

    /**
     * Simply selects the home view to render by returning its name.
     */
    @RequestMapping(value = "/", method = {RequestMethod.GET, RequestMethod.POST})
    public String home(Locale locale, Model model) {
        logger.info("Welcome home! The client locale is {}.", locale);

        Date date = new Date();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG,
                DateFormat.LONG, locale);

        String formattedDate = dateFormat.format(date);

        model.addAttribute("serverTime", formattedDate);

        return "welcome/home";
    }

    @RequestMapping(value = "/secured", method = {RequestMethod.GET, RequestMethod.POST})
    public String seccured(Locale locale, Model model, HttpServletRequest request) {
        logger.info("Secured...");

        OidcSecurityContext context = (OidcSecurityContext)request.getAttribute(OidcSecurityContext.class.getName());

        model.addAttribute("IDToken", ToStringBuilder.reflectionToString(context.getIDToken()));
        model.addAttribute("AccessToken", ToStringBuilder.reflectionToString(context.getToken()));

        return "welcome/secured";
    }
}
