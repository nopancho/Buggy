package org.nopancho.api;

import io.jsonwebtoken.*;
import org.apache.http.HttpStatus;
import org.bson.Document;
import org.nopancho.accounting.model.Rights;
import org.nopancho.accounting.model.User;
import org.nopancho.accounting.persistence.UserDao;
import org.nopancho.config.ConfigManager;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

public class SecuredResource extends Resource {

    public static final String AUTHORIZATION = "Authorization";
    public static String API_SECRET = ConfigManager.getConfig().getString("jwt.secret");
    public static ApplicationException UNAUTHORIZED_EXCEPTION = new ApplicationException(HttpStatus.SC_UNAUTHORIZED, "You are not allowed to access the requested Resource");
    public static ApplicationException NOT_EXISTING_RESOURCE = new ApplicationException(HttpStatus.SC_NOT_ACCEPTABLE, "the requested item does not exist");

    @Context
    HttpServletRequest request;

    public enum AccessStructure {
        ORGANISATION, PROJECT, CHANNEL, SUPER
    }

    private boolean compareRights(Rights required, Rights actual) {
        if (required == Rights.NONE) {
            return true;
        }
        if (required == Rights.READ) {
            return (actual == Rights.READ || actual == Rights.WRITE || actual == Rights.ADMIN);
        }
        if (required == Rights.WRITE) {
            return (actual == Rights.WRITE || actual == Rights.ADMIN);
        }
        if (required == Rights.ADMIN) {
            return actual == Rights.ADMIN;
        }
        return false;
    }

    private boolean authenticate(boolean asSuperUser) {
        User userFromRequest = getUserFromRequest();
        if (!asSuperUser) {
            return true;
        }
        Boolean superUser = userFromRequest.getSuperUser();
        if (!superUser) {
            throw this.UNAUTHORIZED_EXCEPTION;
        }
        return true;
    }

    public User getUserFromRequest() {
        String token = request.getHeader("Authorization");
        return getUserByToken(token);
    }

    //    public User getUserByToken(String token) {
    //        if(token == null) {
    //            throw this.UNAUTHORIZED_EXCEPTION;
    //        }
    //        if (token != null) {
    //            token = token.replace("Bearer ", "");
    //            try {
    //                try {
    //                    Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(API_SECRET))
    //                            .parseClaimsJws(token).getBody();
    //                    // Additional validation
    //                } catch (ExpiredJwtException e) {
    //                    throw new ApplicationException(HttpStatus.SC_UNAUTHORIZED, "Token has expired");
    //                } catch (SignatureException e) {
    //                    throw new ApplicationException(HttpStatus.SC_UNAUTHORIZED, "Invalid token signature");
    //                } catch (MalformedJwtException e) {
    //                    throw new ApplicationException(HttpStatus.SC_UNAUTHORIZED, "Malformed token");
    //                }
    //                Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(API_SECRET))
    //                        .parseClaimsJws(token).getBody();
    //                Object o = claims.get(User.ID);
    //                if (o == null) {
    //                    throw this.UNAUTHORIZED_EXCEPTION;
    //                }
    //                Integer userId = (Integer) o;
    //                User user = UserDao.getInstance().readOne(userId);
    //                return user;
    //            } catch (Exception e) {
    //                System.out.println(token);
    //                e.printStackTrace();
    //                throw this.UNAUTHORIZED_EXCEPTION;
    //            }
    //        }
    //        throw this.UNAUTHORIZED_EXCEPTION;
    //    }

    public User getUserByToken(String token) {
        if (token == null) {
            throw this.UNAUTHORIZED_EXCEPTION; // Reject null tokens immediately
        }

        // Remove "Bearer " prefix if present
        token = token.replace("Bearer ", "");

        try {
            // Parse and validate the token
            Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(API_SECRET)) // Validate signature
                    .parseClaimsJws(token).getBody();

            // Validate required claims
            validateClaims(claims);

            String subject = claims.getSubject();
            if (subject == null) {
                throw new ApplicationException(HttpStatus.SC_UNAUTHORIZED, "Invalid token (subject is missing)");
            }

            // Convert the User ID to Integer and fetch the user from the database
            Integer userId = Integer.valueOf(subject);
            User user = UserDao.getInstance().readOne(userId);
            if (user == null) {
                throw new ApplicationException(HttpStatus.SC_UNAUTHORIZED, "Could not match user to token");
            }
            return user;
        } catch (ExpiredJwtException e) {
            throw new ApplicationException(HttpStatus.SC_UNAUTHORIZED, "Token has expired");
        } catch (SignatureException e) {
            throw new ApplicationException(HttpStatus.SC_UNAUTHORIZED, "Invalid token signature");
        } catch (MalformedJwtException e) {
            throw new ApplicationException(HttpStatus.SC_UNAUTHORIZED, "Malformed token");
        } catch (Exception e) {
            // Log and throw a generic unauthorized exception for unexpected errors
            System.err.println("Token validation failed: " + token);
            e.printStackTrace();
            throw this.UNAUTHORIZED_EXCEPTION;
        }
    }

    /**
     * Validates additional claims in the token for enhanced security.
     * Throws an exception if any validation fails.
     */
    private void validateClaims(Claims claims) {
        // Validate expiration time (exp)
        Date expiration = claims.getExpiration();
        if (expiration == null || expiration.before(new Date())) {
            throw new ApplicationException(HttpStatus.SC_UNAUTHORIZED, "Token has expired");
        }

        // Validate issuer (iss)
        String issuer = claims.getIssuer();
        if (!"your-application-identifier".equals(issuer)) {
            throw new ApplicationException(HttpStatus.SC_UNAUTHORIZED, "Invalid token issuer");
        }

        // Validate audience (aud) if necessary
        String audience = claims.getAudience();
        if (audience == null || !"your-audience-identifier".equals(audience)) {
            throw new ApplicationException(HttpStatus.SC_UNAUTHORIZED, "Invalid token audience");
        }
    }

    public String createToken(User user) {
        // The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        // We will sign our JWT with our ApiKey secret
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(API_SECRET);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        // Set standard claims
        JwtBuilder builder = Jwts.builder().setIssuedAt(now)                         // `iat` claim
                .setSubject(user.getId().toString())      // `sub` claim (User ID)
                .setIssuer("your-application-identifier") // `iss` claim
                .setAudience("your-audience-identifier")  // `aud` claim
                .signWith(signatureAlgorithm, signingKey);

        // Add custom claims
        builder.claim(User.EMAIL, user.getEmail());

        // set the subject of the token to be the user id
        builder.setSubject(user.getId().toString());

        // Set expiration time (1 hour in this case)
        long ttlMillis = 3600000; // 1 hour
        if (ttlMillis > 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp); // `exp` claim
        }

        // Add JWT ID (`jti`) for revocation tracking
        builder.setId(UUID.randomUUID().toString());

        // Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }

    public static Response success(String message, Document data) {
        if (data == null) {
            data = new Document();
        }
        data.put("message", message);
        return Response.ok().entity(data.toJson()).build();
    }


    // Error response
    public static Response error(int httpStatus, String message, Document data) {
        if (data == null) {
            data = new Document();
        }
        data.put("message", message);
        return Response.status(httpStatus).entity(data.toJson()).build();
    }
}
