/**
 * The MIT License
 * Copyright (c) 2015 Population Register Centre
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.vm.kapa.identification.shibboleth.extauthn.authn;

import fi.vm.kapa.identification.shibboleth.extauthn.context.FakeContext;
import fi.vm.kapa.identification.shibboleth.extauthn.context.FakeEidasContext;
import fi.vm.kapa.identification.shibboleth.extauthn.context.FakeForeignContext;
import fi.vm.kapa.identification.type.AuthMethod;
import net.shibboleth.idp.authn.ExternalAuthentication;
import net.shibboleth.idp.authn.ExternalAuthenticationException;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.RequestedPrincipalContext;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;
import org.apache.commons.lang.StringUtils;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@PropertySource(value = "file:///opt/identity-provider/fake-identity-provider.properties")
public class ApacheAuthnHandler {

    @Autowired
    private Environment env;

    private static final Logger logger = LoggerFactory.getLogger(ApacheAuthnHandler.class);

    private static final String COOKIE_COUNTRY_NAME = "E-Identification-Country";

    public void initialize(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws ServletException, IOException {
        try {
            final String key = ExternalAuthentication.startExternalAuthentication(httpRequest);

            ProfileRequestContext prc = ExternalAuthentication.getProfileRequestContext(key, httpRequest);
            AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class);
            String contextClassRef = resolveRequestedAuthenticationContextClass(prc);

            if ( isForeignIdentification(prc) ) {
                String[] userData = httpRequest.getParameter("foreign_identity").split(";");
                ac.addSubcontext( new FakeForeignContext(userData[0], userData[1], userData[2], userData[3]));
                logger.debug("Foreign authentication");
            }
            else if (isEidasRequest(prc)) {
                String[] userData = getUserDataByCookie(httpRequest);
                ac.addSubcontext(new FakeEidasContext(userData[0], userData[1], userData[2], userData[3]));
                if ( userData.length > 4 ) {
                    contextClassRef = userData[4];
                }
                logger.debug("eIDAS authentication");
            } else {
                String satu = httpRequest.getParameter("satu");
                String hetu = httpRequest.getParameter("hetu");

                String issuerCN = null, cn = null;
                boolean mimicHstIdp = showSatu(prc);

                // Three test cases:
                // 1. Test Idp; return hetu + cn
                // 2. Mimic HST card; return satu + issuerCN
                // 3. Mimic organization card; return hetu

                if (!mimicHstIdp && StringUtils.isNotBlank(hetu) && hetu.length() > 6) {
                    cn = "Testihenkilö " + hetu.substring(0, 6);
                } else if (mimicHstIdp && StringUtils.isNotBlank(satu)) {
                    issuerCN = "VRK CA for Test Purposes - G2";
                }

                ac.addSubcontext(new FakeContext(satu, hetu, issuerCN, cn));
                logger.debug("SATU: " + satu + ", HETU: " + hetu + ", issuerCN: " + issuerCN + ", CN: " + cn);
            }

            Set<Principal> principals = new HashSet<>();
            principals.add(new UsernamePrincipal("FAKE"));
            principals.add(new AuthnContextClassRefPrincipal(contextClassRef));

            Subject subject = new Subject();
            subject.getPrincipals().addAll(principals);
            httpRequest.setAttribute(ExternalAuthentication.SUBJECT_KEY, subject);

            ExternalAuthentication.finishExternalAuthentication(key, httpRequest, httpResponse);

        } catch (final ExternalAuthenticationException e) {
            throw new ServletException("Error processing external authentication request", e);
        }
    }

    private String resolveRequestedAuthenticationContextClass(ProfileRequestContext profileRequestContext) throws ExternalAuthenticationException {
        AuthenticationContext ac = profileRequestContext.getSubcontext(AuthenticationContext.class);
        RequestedPrincipalContext rpc = ac.getSubcontext(RequestedPrincipalContext.class);
        if (rpc != null) {
            for (Principal principal : rpc.getRequestedPrincipals()) {
                if (principal instanceof AuthnContextClassRefPrincipal) {
                    String authenticationContextOid = principal.getName();
                    logger.debug("Requested principal: " + authenticationContextOid);
                    for (AuthMethod authMethod : AuthMethod.values()) {
                        if (authMethod.getOidValue().contentEquals(authenticationContextOid)) {
                            logger.debug("Requested oid friendly name: " + authMethod.toString());
                            return authenticationContextOid;
                        }
                    }
                }
            }
        }
        throw new ExternalAuthenticationException("No valid requested context class found");
    }

    public static boolean showSatu(ProfileRequestContext prc) {
        AuthnRequest message = (AuthnRequest) prc.getInboundMessageContext().getMessage();
        if( message.getRequestedAuthnContext() != null) {
            List<AuthnContextClassRef> authnContextClassRefs = message.getRequestedAuthnContext().getAuthnContextClassRefs();
            if( !CollectionUtils.isEmpty(authnContextClassRefs) ) {
                String classRef = authnContextClassRefs.get(0).getAuthnContextClassRef();
                return AuthMethod.TESTIDP.getOidValue().equals(classRef) || AuthMethod.fLoA3.getOidValue().equals(classRef);
            }
        }
        return false;
    }

    public static boolean isForeignIdentification(ProfileRequestContext prc) {
        AuthnRequest message = (AuthnRequest) prc.getInboundMessageContext().getMessage();
        if( message.getRequestedAuthnContext() != null) {
            List<AuthnContextClassRef> authnContextClassRefs = message.getRequestedAuthnContext().getAuthnContextClassRefs();
            if( !CollectionUtils.isEmpty(authnContextClassRefs) ) {
                String classRef = authnContextClassRefs.get(0).getAuthnContextClassRef();
                return AuthMethod.FFI.getOidValue().equals(classRef);
            }
        }
        return false;
    }

    public static boolean isEidasRequest(ProfileRequestContext prc) {
        AuthnRequest message = (AuthnRequest) prc.getInboundMessageContext().getMessage();
        if( message.getRequestedAuthnContext() != null) {
            List<AuthnContextClassRef> authnContextClassRefs = message.getRequestedAuthnContext().getAuthnContextClassRefs();
            if( !CollectionUtils.isEmpty(authnContextClassRefs) ) {
                String classRef = authnContextClassRefs.get(0).getAuthnContextClassRef();
                return AuthMethod.eLoA2.getOidValue().equals(classRef) || AuthMethod.eLoA3.getOidValue().equals(classRef);
            }
        }
        return false;
    }

    private Cookie getCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    private String[] getUserDataByCookie(HttpServletRequest httpRequest) {
        Cookie cookie = getCookie(httpRequest, COOKIE_COUNTRY_NAME);
        if ( cookie != null && cookie.getValue() != null ) {
            String data = env.getProperty("country."+cookie.getValue().toUpperCase());
            if ( data != null ) {
                return data.split(";");
            }
        }
        // default values
        return new String [] {"", "", "", ""};
    }
}
