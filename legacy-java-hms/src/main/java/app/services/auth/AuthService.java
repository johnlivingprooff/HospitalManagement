package app.services.auth;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.daos.patient.PatientDao;
import app.models.Subject;
import app.models.account.Account;
import app.models.patient.Patient;
import app.services.user.AccountService;
import app.util.DateUtils;
import app.util.LocaleUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;

@ServiceDescriptor
public final class AuthService extends ServiceImpl {
    public static final int RESET_TOKEN_TTL_MINUTES = 5;

    private static final int BCRYPT_HASH_MIN_ROUNDS = 12;
    private static final int BCRYPT_HASH_MAX_ROUNDS = 30;

    public AuthService(Configuration configuration) throws Exception {
        super(configuration);
        if (!LocaleUtil.withinBounds(configuration.PasswordHashRounds, BCRYPT_HASH_MIN_ROUNDS, BCRYPT_HASH_MAX_ROUNDS)) {
            throw new Exception("Password hashing rounds must be at least 12 and no more than 36");
        }
    }

    /**
     * <p>Hash password using defined rounds</p>
     *
     * @param plainText Plain text password
     * @return Hashed password
     */
    public String hashPassword(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt(getSystemConfiguration().PasswordHashRounds));
    }

    /**
     * <p>Verify the password match</p>
     *
     * @param plainText Plain text password
     * @param hashed    Hashed password
     * @return true if the passwords match
     */
    public boolean verifyPassword(String plainText, String hashed) {
        return BCrypt.checkpw(plainText, hashed);
    }

    /**
     * <p>This method generates a password reset token for the given subject. This token expires in {@link #RESET_TOKEN_TTL_MINUTES}  minutes</p>
     * <p>The token is cryptographically signed signed using the subject's password.
     * Once the subject's password changes or the token expires, it becomes invalid</p>
     *
     * @param subject The subject whose reset token to generate
     * @return token
     */
    public String createPasswordResetToken(Subject subject) {
        String secret;
        LocalDateTime iat;
        LocalDateTime exp;

        iat = LocalDateTime.now();
        exp = iat.plusMinutes(RESET_TOKEN_TTL_MINUTES);

        secret = getSubjectSecret(subject);

        return JWT.create()
                .withIssuedAt(DateUtils.localDateTimeToDate(iat))
                .withIssuer(getSystemConfiguration().ServerName)
                .withClaim("type", subject.getSubjectType().name())
                .withSubject(Long.toHexString(subject.getId()))
                .withExpiresAt(DateUtils.localDateTimeToDate(exp))
                .sign(Algorithm.HMAC256(secret));
    }

    public boolean verifyPasswordResetToken(String token, Subject subject) {
        String secret;
        JWTVerifier verifier;

        secret = getSubjectSecret(subject);
        try {
            verifier = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer(getSystemConfiguration().ServerName)
                    .withSubject(Long.toHexString(subject.getId()))
                    .withClaim("type", subject.getSubjectType().name())
                    .build();
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            getLogger().error("Invalid password reset JWT for subject {}({})",
                    subject.getFullName(), subject.getEmail());
            return false;
        }
    }

    private String getSubjectSecret(Subject subject) {
        return subject.getPassword() + subject.getCreated().getTime();
    }

    public void updateSubjectPassword(String password, Subject subject) {
        Date date = new Date();
        if (subject.getSubjectType() == Subject.SubjectType.STA && (subject instanceof Account)) {
            ((Account) subject).setPassword(hashPassword(password));
            ((Account) subject).setModified(date);
            getService(AccountService.class).updateAccount((Account) subject);
        } else if (subject.getSubjectType() == Subject.SubjectType.STP && (subject instanceof Patient)) {
            ((Patient) subject).setPassword(hashPassword(password));
            ((Patient) subject).setModified(date);
            withDao(PatientDao.class).updatePatient((Patient) subject);
        } else {
            getLogger().warn("Skipped password update for an unknown account type " + subject.toString());
        }
    }

    public String generatePassword() {
        byte[] passwordBytes;
        SecureRandom secureRandom;

        secureRandom = new SecureRandom();
        passwordBytes = new byte[15];
        secureRandom.nextBytes(passwordBytes);

        return hashPassword(new String(passwordBytes));
    }
}
