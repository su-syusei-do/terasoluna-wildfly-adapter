# Terasoluna Serverframework for java + Keycloak + Wildfly Adapter

## JDK

Amazon Correto 11<br>
https://docs.aws.amazon.com/ja_jp/corretto/latest/corretto-11-ug/downloads-list.html

## Keycloak

Keycloak 21.0.1<br>
https://www.keycloak.org/downloads

~~~
set JAVA_HOME=C:\work\jdk11.0.18_10
set KEYCLOAK_ADMIN=admin
set KEYCLOAK_ADMIN_PASSWORD=password
bin/kc.bat start-dev
~~~

http://localhost:8080/

## WildFly

WildFly 26.1.3.Final<br>
https://www.wildfly.org/downloads/

standalone.xml
~~~
<socket-binding name="http" port="${jboss.http.port:8180}"/>
~~~

**Wildfly 27はJakarta EE 10、Terasoluna Serverframeworkはこれに対応していない**

### 起動

~~~
set JAVA_HOME=C:\work\jdk11.0.18_10
bin\standalone.bat
~~~

### 管理ユーザを追加する

~~~
set JAVA_HOME=C:\work\jdk11.0.18_10
bin\add-user.bat
~~~

admin/passw0rd-

## Terasoluna server framework for java sample app

mvn archetype:generate -DarchetypeGroupId=org.terasoluna.gfw.blank -DarchetypeArtifactId=terasoluna-gfw-web-blank-archetype -DarchetypeVersion=5.7.1.SP1.RELEASE

### web.xml

web.xmlからspring securityに関する記述を削除する。
Wildfly adapterに関する記述を追加する。

~~~
    <!-- oidc.json -->
    <security-constraint>
        <web-resource-collection>
            <web-resource-name>secured</web-resource-name>
            <url-pattern>/secured</url-pattern>
        </web-resource-collection>
        <auth-constraint>
            <role-name>*</role-name>
        </auth-constraint>   
    </security-constraint>

    <login-config>
        <auth-method>OIDC</auth-method>
    </login-config>

    <security-role>
        <role-name>*</role-name>
    </security-role>
~~~

### oidc.json

/WEB-INF/oidc.jsonを追加する

~~~
{
    "client-id" : "sample-web-app",
    "provider-url" : "${env.OIDC_PROVIDER_URL:http://localhost:8080}/realms/sample1",
    "credentials" : {
        "secret" : "EogqMI6xFELBpLNNLdmotqv4z7TZyxk8"
    },
    "principal-attribute" : "preferred_username",
    "ssl-required" : "EXTERNAL"
}
~~~

### Terasoluna ServerFramework for Javaを利用するときは Wildfly 26を利用すること

Jakarta EE 8に対応している。Jakarta EE 10は未対応

### url

http://localhost:8180/SampleWebApp/
http://localhost:8180/SampleWebApp/secured

### IDToken, AccessTokenの取得

Wildfly Adapterを利用するとき、以下の方法で取得できる

~~~
import org.wildfly.security.http.oidc.*;
import org.apache.commons.lang3.builder.*;


    @RequestMapping(value = "/secured", method = {RequestMethod.GET, RequestMethod.POST})
    public String seccured(Locale locale, Model model, HttpServletRequest request) {
        logger.info("Secured...");

        OidcSecurityContext context = (OidcSecurityContext)request.getAttribute(OidcSecurityContext.class.getName());

        model.addAttribute("IDToken", ToStringBuilder.reflectionToString(context.getIDToken()));
        model.addAttribute("AccessToken", ToStringBuilder.reflectionToString(context.getToken()));

        return "welcome/secured";
    }
~~~

pom.xml
~~~
        <dependency>
            <groupId>org.wildfly.security</groupId>
            <artifactId>wildfly-elytron-http-oidc</artifactId>
            <version>2.1.0.Final</version>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.12.0</version>
        </dependency>
~~~