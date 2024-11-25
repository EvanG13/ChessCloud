package org.example.utils;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ResendUtil {

  public static void sendEmail(String to, String subject, String body) {
    Resend resend = new Resend(DotenvClass.dotenv.get("RESEND_API_KEY"));
    String from = String.format(
        "%s <%s>",
        DotenvClass.dotenv.get("RESEND_FROM_NAME"),
        DotenvClass.dotenv.get("RESEND_FROM_EMAIL")
    );

    CreateEmailOptions params = CreateEmailOptions.builder()
        .from(from)
        .to(to)
        .subject(subject)
        .html(body)
        .build();

    try {
      CreateEmailResponse data = resend.emails().send(params);
    }
    catch (Exception e) {
      // Possible errors: https://resend.com/docs/api-reference/errors
      System.out.println(e.getMessage());
    }
  }

  public static void sendVerificationEmail(String to, String token) {
    String verificationUrl = String.format("%s/verify?token=%s", DotenvClass.dotenv.get("FRONTEND_URL"), token);

    sendEmail(
        to,
        "Email Verification Required",
        String.format("<h1>Verify your email <a href='%s'>here</a></h1>", verificationUrl)
    );
  }

  public static void sendPasswordResetEmail(String to, String token) {
    String resetUrl = String.format("%s/resetPassword?token=%s&email=%s", DotenvClass.dotenv.get("FRONTEND_URL"), token, to);

    sendEmail(
        to,
        "Password Reset Requested",
        String.format("<h1>Reset your password <a href='%s'>here.</a></h1>", resetUrl)
    );
  }
}
