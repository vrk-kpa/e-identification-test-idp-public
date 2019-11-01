<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="net.shibboleth.idp.authn.ExternalAuthentication" %>
<%@ page import="org.opensaml.profile.context.ProfileRequestContext" %>
<%@ page import="fi.vm.kapa.identification.shibboleth.extauthn.authn.ApacheAuthnHandler" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
    final ProfileRequestContext prc = ExternalAuthentication.getProfileRequestContext(request.getParameter(ExternalAuthentication.CONVERSATION_KEY), request);
    boolean showSatu = ApacheAuthnHandler.showSatu(prc);
    boolean directSubmit = ApacheAuthnHandler.isEidasRequest(prc);
    boolean foreignSubmit = ApacheAuthnHandler.isForeignIdentification(prc);
%>

<!doctype html>
<!--[if lte IE 7]> <html lang="fi" itemtype="http://schema.org/WebPage" class="no-js lte_ie9 lte_ie8 lte7"> <![endif]-->
<!--[if IE 8]> <html lang="fi" itemtype="http://schema.org/WebPage" class="no-js lte_ie9 lte_ie8 ie8"> <![endif]-->
<!--[if IE 9]> <html lang="fi" itemtype="http://schema.org/WebPage" class="no-js lte_ie9 ie9"> <![endif]-->
<!--[if gt IE 9]><!--><html lang="fi" itemtype="http://schema.org/WebPage" class="no-js"><!--<![endif]-->
<head>
    <meta charset="utf-8">
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <title>Testitunnistaja</title>
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="/resources/stylesheets/style.css">
    <script src="/resources/js/vendor/modernizr-2.8.3.min.js"></script>
    <script src="/resources/js/vendor/jquery.min.js"></script>
    <script src="/resources/js/plugins.js"></script>
    <script src="/resources/js/main.js"></script>
    <script src="/resources/js/vendor/js.cookie.js"></script>

    <!--[if lt IE 9]>
    <script src="/resources/js/vendor/respond.js"></script>
    <![endif]-->
    <style>
        #page-header, #main, #page-footer {
            display: none;
        }
        .label-row {
            position: relative;
            margin-bottom: 5px;
        }
        .default-link {
            position: absolute;
            bottom: 0;
            right: 0;
            font-size: 1.4rem;
        }
        #tunnistaudu {
            max-width: 300px;
            margin-top:15px;
            margin-bottom:10px;
        }
        .validation-error {
            margin-top: 5px;
            font-size: 1.6rem;
        }
        @media(max-width: 600px) {
            .instructions li {
                font-size: 1.6rem;
                line-height: 2.4rem;
                margin-top: .8rem;
                margin-bottom: 1.6rem;
            }
            .validation-error {
                font-size: 1.4rem;
            }
        }
    </style>
</head>
<body id="identification-service" class="txlive">
<a href="#main" class="visuallyhidden focusable">Siirry suoraan sisältöön</a>
<header id="page-header" role="banner">
    <div id="header-content" class="container">
        <h1 id="suomi.fi-tunnistaminen" class="site-logo">
            <img src="/resources/img/tunnistaminen_logo_fi.svg" title="Suomi.fi-tunnistus" />
            <span class="visuallyhidden">Suomi.fi-tunnistus</span>
        </h1>
    </div>
</header>
<main id="main" role="main" name="main">
    <div class="main hst-idp-page">
        <div class="container">
            <h2><%= showSatu ? "Test IdP" : "Testitunnistaja"%></h2>
            <p>Testitunnistajan avulla voit tunnistautua Suomi.fi-tunnistuksen testiympäristössä valitsemallasi testitunnisteella (henkilötunnus/SATU/Ulkomaalaisen tunniste).</p>
            <p>Löydät lisätietoja käytettävissä olevista testitunnisteista <a target="_blank" href="https://palveluhallinta.suomi.fi/fi/tuki/artikkelit/5a82ef7ab03cdc41de664a2b">Palveluhallinnasta</a>.</p>
            <ul class="instructions">
                <li>1. Syötä alla olevaan kenttään vaadittu testitunniste (henkilötunnus/SATU) tai valitse ulkomaalaisen identiteetti</li>
                <li>2. Valitse "Tunnistaudu"</li>
            </ul>
            <div class="row">
                <div class="col-xs-12 col-md-8">
                    <div class="box hst-identification-info">
                        <div class="row">
                            <form id="login-form" action="<%= request.getContextPath() %>/authn/External" method="post">
                                <input type="hidden" name="<%= ExternalAuthentication.CONVERSATION_KEY %>"
                                       value="<c:out value="<%= request.getParameter(ExternalAuthentication.CONVERSATION_KEY) %>" />">

                                <% if ( foreignSubmit ) { %>
                                <div class="input-container">
                                    <input type="radio" id="1" name="foreign_identity" value="7000791435;Max;Mustermann;1970-01-01" checked />
                                    <label for="1">7000791435, Max Mustermann, 1970-01-01</label><br>
                                    <input type="radio" id="2" name="foreign_identity" value="700020880P;Anders;Andersen;1971-02-01"/>
                                    <label for="2">700020880P, Anders Andersen, 1971-02-01</label><br>
                                    <input type="radio" id="3" name="foreign_identity" value="700064221T;Janina;Kowalska;1972-05-01"/>
                                    <label for="3">700064221T, Janina Kowalska, 1972-05-01</label><br>
                                    <input type="radio" id="4" name="foreign_identity" value="700040194R;Janez;Novak;1973-01-09"/>
                                    <label for="4">700040194R, Janez Novak, 1973-01-09</label><br>
                                    <input type="radio" id="5" name="foreign_identity" value="700031284A;Елена;Вяльбе;2000-05-05"/>
                                    <label for="5">700031284A, Елена Вяльбе, 2000-05-05</label><br>
                                </div>
                                <% } else {
                                      if ( showSatu ) { %>
                                <div class="input-container">
                                    <div class="label-row">
                                        <label for="satu_input" class="form-label strong small" style="margin-top:0">SATU</label>
                                        <span class="default-link"><a id="satu_default" href="#">Käytä oletusta 99799028388</a></span>
                                    </div>
                                    <input id="satu_input" type="text" name="satu" placeholder="99799028388"/>
                                </div>
                                <%     } %>
                                <div class="input-container">
                                    <div class="label-row">
                                        <label for="hetu_input" class="form-label strong small" style="margin-top:0">Henkilötunnus</label>
                                        <span class="default-link"><a id="hetu_default" href="#">Käytä oletusta 210281-9988</a></span>
                                    </div>
                                    <input id="hetu_input" type="text" name="hetu" placeholder="210281-9988"/>
                                </div>
                                <% } %>
                            </form>
                            <form id="login-cancel" action="<%= request.getContextPath() %>/authn/Error" method="post">
                                <input type="hidden" name="<%= ExternalAuthentication.CONVERSATION_KEY %>"
                                       value="<c:out value="<%= request.getParameter(ExternalAuthentication.CONVERSATION_KEY) %>" />">
                                <input type="hidden" name="error" value="cancel">
                            </form>
                            <span id="hetu-error" class="validation-error" aria-live="polite" style="visibility: hidden">Väärän muotoinen henkilötunnus</span>
                            <button id="tunnistaudu" data-i18n="hst__tunnistaudu" disabled>Tunnistaudu</button>
                            <a id="cancel" href="#" onclick="javascript:$('#login-cancel').submit();return false;" class="button-cancel" style="margin-top:15px">Keskeytä</a>

                            <script>
                                $(document).ready(function() {
                                    if ( <%= directSubmit %> ) {
                                        $("#login-form").submit();
                                    }
                                    else {
                                        $("#page-header").show();
                                        $("#main").show();
                                        $("#page-footer").show();
                                    }
                                    if ( <%= foreignSubmit %> ) {
                                        $('#tunnistaudu').prop('disabled', false);
                                    }
                                    function checkInputs() {
                                        var oneFilled = false;
                                        $('input[type=text]').each(function() {
                                            if($(this).val() !== '') {
                                               oneFilled = true;
                                            }
                                        });
                                        $('#tunnistaudu').prop('disabled', !oneFilled);
                                    }

                                    function satuExistsAndFilled() {
                                        let $satu = $("#satu_input");
                                        return $satu.lenght() !== 0 && $satu.val() !== '';
                                    }

                                    function validateHetuFormat() {
                                        var hetuRegexp = /^\d{6}[+-A]\d{3}[0-9ABCDEFHJKLMNPRSTUVWXY]$/;
                                        var hetu = $('#hetu_input').val();
                                        if (hetu !== '' && hetuRegexp.test(hetu)) {
                                            $('#hetu-error').css("visibility", "hidden");
                                            return true;
                                        } else {
                                            $('#hetu-error').css("visibility", "visible");
                                            return false;
                                        }
                                    }

                                    $('input[type=text]').keyup(checkInputs).focusout(function() {
                                        checkInputs();
                                    });

                                    $("#hetu_input").focusout(function() {
                                        validateHetuFormat();
                                    });

                                    $("#tunnistaudu").click(function() {
                                        if (satuExistsAndFilled || validateHetuFormat()) {
                                            $("#login-form").submit();
                                        }
                                    });

                                    $("#satu_default").click(function() {
                                        $("#satu_input").val("99799028388");
                                        checkInputs();
                                    });

                                    $("#hetu_default").click(function() {
                                        $("#hetu_input").val("210281-9988");
                                        checkInputs();
                                        validateHetuFormat();
                                    });
                                });
                            </script>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <a  class="go-back" title="Peruuta ja palaa tunnistusvälineen valintaan" href="#"
                    onclick="javascript:$('#login-cancel').submit();return false;">Palaa tunnistusvälineen valintaan</a>
            </div>
        </div>
    </div>
</main>
<footer id="page-footer" role="contentinfo">
    <a href="#" class="go-up">Takaisin ylös</a>
    <div id="footer-content" class="container">
        <span class="site-logo">
            <img src="/resources/img/tunnistaminen_logo_fi.svg" title="Suomi.fi-tunnistus" />
        </span>
    </div>
</footer>
</body>
</html>
