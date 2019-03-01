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
import net.shibboleth.idp.authn.ExternalAuthentication;
import net.shibboleth.idp.authn.ExternalAuthenticationException;
import net.shibboleth.idp.authn.context.AuthenticationContext;
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

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

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

            FakeContext hcc;
            if ( isEidasRequest(prc) ) {
                String[] userData = getUserDataByCookie(httpRequest);
                hcc = new FakeContext(null, null ,null, null, userData[0], userData[1], userData[2], userData[3]);
                logger.debug("eIDAS authentication");
            }
            else {
                String satu = httpRequest.getParameter("satu");
                String hetu = httpRequest.getParameter("hetu");

                String issuerCN = null, cn = null;
                boolean mimicHstIdp = showSatu(prc);

                // Three test cases:
                // 1. Test Idp; return hetu + cn
                // 2. Mimic HST card; return satu + issuerCN
                // 3. Mimic organization card; return hetu

                if ( !mimicHstIdp && StringUtils.isNotBlank(hetu) && hetu.length() > 6 ) {
                    cn =  "Testihenkilö " + hetu.substring(0, 6);
                }
                else if ( mimicHstIdp && StringUtils.isNotBlank(satu) ) {
                    issuerCN = "VRK CA for Test Purposes - G2";
                }

                hcc = new FakeContext(satu, hetu, issuerCN, cn, null, null, null, null);
                logger.debug("SATU: "+satu+", HETU: "+hetu+", issuerCN: "+issuerCN+", CN: "+cn);
            }

            AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class);
            ac.addSubcontext(hcc);
            httpRequest.setAttribute(ExternalAuthentication.PRINCIPAL_NAME_KEY, "FAKE");
            ExternalAuthentication.finishExternalAuthentication(key, httpRequest, httpResponse);

        } catch (final ExternalAuthenticationException e) {
            throw new ServletException("Error processing external authentication request", e);
        }
    }

    public static boolean showSatu(ProfileRequestContext prc) {
        AuthnRequest message = (AuthnRequest) prc.getInboundMessageContext().getMessage();
        if( message.getRequestedAuthnContext() != null) {
            List<AuthnContextClassRef> authnContextClassRefs = message.getRequestedAuthnContext().getAuthnContextClassRefs();
            if( !CollectionUtils.isEmpty(authnContextClassRefs) ) {
                String classRef = authnContextClassRefs.get(0).getAuthnContextClassRef();
                return "urn:oid:1.2.246.517.3002.110.995".equals(classRef) || "http://ftn.ficora.fi/2017/loa3".equals(classRef);
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
                return "http://eidas.europa.eu/LoA/high".equals(classRef) || "http://eidas.europa.eu/LoA/substantial".equals(classRef);
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
