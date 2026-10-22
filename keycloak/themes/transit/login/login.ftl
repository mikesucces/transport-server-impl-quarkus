<#import "template.ftl" as layout>
<#import "field.ftl" as field>
<#import "buttons.ftl" as buttons>
<#import "social-providers.ftl" as identityProviders>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
<!-- template: login.ftl -->

    <#if section = "header">
        ${msg("loginAccountTitle")}
    <#elseif section = "form">
        <div id="kc-form">
          <div id="kc-form-wrapper">
            <#if realm.password>
                <form id="kc-form-login" class="${properties.kcFormClass!}" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post" novalidate="novalidate">
                    <#if !usernameHidden??>
                        <#assign label>
                            <#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if>
                        </#assign>
                        <@field.input name="username" label=label autofocus=true autocomplete="username" value=login.username!'' />
                    </#if>

                    <@field.password name="password" label=msg("password") forgotPassword=realm.resetPasswordAllowed autofocus=usernameHidden?? autocomplete="current-password" />

                    <div class="${properties.kcFormGroupClass!}">
                        <#if realm.rememberMe && !usernameHidden??>
                            <@field.checkbox name="rememberMe" label=msg("rememberMe") value=login.rememberMe?? />
                        </#if>
                    </div>

                    <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                    <@buttons.loginButton />
                </form>
                <#-- Encadre des comptes de demonstration (theme Transit, developpement uniquement) -->
                <#if (properties.showDemoAccounts!"false") == "true">
                <div class="transit-demo" id="transit-demo">
                    <div class="transit-demo__title">Comptes de démonstration</div>
                    <p class="transit-demo__hint">Mot de passe commun : <code>Transit2026!</code></p>
                    <ul class="transit-demo__list">
                        <li><button type="button" data-user="admin"><strong>admin</strong><span>Propriétaire</span></button></li>
                        <li><button type="button" data-user="manager"><strong>manager</strong><span>Gestionnaire</span></button></li>
                        <li><button type="button" data-user="controller"><strong>controller</strong><span>Contrôleur</span></button></li>
                        <li><button type="button" data-user="driver"><strong>driver</strong><span>Chauffeur</span></button></li>
                    </ul>
                    <p class="transit-demo__hint">Cliquer sur un compte remplit le formulaire.</p>
                </div>
                <script>
                    document.querySelectorAll('#transit-demo button[data-user]').forEach(function (b) {
                        b.addEventListener('click', function () {
                            var u = document.getElementById('username'), p = document.getElementById('password');
                            if (u) { u.value = b.getAttribute('data-user'); }
                            if (p) { p.value = 'Transit2026!'; }
                            var go = document.getElementById('kc-login');
                            if (go) { go.focus(); }
                        });
                    });
                </script>
                </#if>
            </#if>
            </div>
        </div>
    <#elseif section = "info" >
        <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
            <div id="kc-registration-container" class="${properties.kcLoginFooterBand!}">
                <div id="kc-registration" class="${properties.kcLoginFooterBandItem!}">
                    <span>${msg("noAccount")} <a href="${url.registrationUrl}">${msg("doRegister")}</a></span>
                </div>
            </div>
        </#if>
    <#elseif section = "socialProviders" >
        <#if realm.password && social.providers?? && social.providers?has_content>
            <@identityProviders.show social=social/>
        </#if>
    </#if>

</@layout.registrationLayout>
