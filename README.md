# Terasoluna Serverframework for java + Keycloak + Wildfly Adapter

## JDK

### Amazon Correto 11
https://docs.aws.amazon.com/ja_jp/corretto/latest/corretto-11-ug/downloads-list.html

## Keycloak

### Keycloak 21.0.1
https://www.keycloak.org/downloads

~~~
set JAVA_HOME=C:\work\jdk11.0.18_10
set KEYCLOAK_ADMIN=admin
set KEYCLOAK_ADMIN_PASSWORD=password
bin/kc.bat start-dev
~~~

http://localhost:8080/

## WildFly

### WildFly 26.1.3.Final
https://www.wildfly.org/downloads/

Keycloakとのポート競合を避けるための編集。

#### standalone.xml
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

環境を準備する。

~~~
export JAVA_HOME=/c/work/jdk11.0.18_10
export MAVEN_HOME=/c/work/apache-maven-3.9.1
export PATH=$PATH:$JAVA_HOME/bin:$MAVEN_HOME/bin
~~~

ブランクプロジェクトを作成する。

~~~
mvn archetype:generate -DarchetypeGroupId=org.terasoluna.gfw.blank -DarchetypeArtifactId=terasoluna-gfw-web-blank-archetype -DarchetypeVersion=5.7.1.SP1.RELEASE
~~~

### web.xml

web.xmlからspring securityに関する記述を削除する。<br>
Wildfly adapterに関する記述を追加する。<br>
以下のサンプルでは`/secured`以下は認証が必要。

~~~
    <!-- 追加する定義 -->
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

### ログアウト

`http://auth-server/realms/{realm-name}/protocol/openid-connect/logout`

post_logout_redirect_uriパラメータでログアウト後の遷移先を指定する。

client_idを一緒に指定すること。

#### src/main/webapp/WEB-INF/views/welcome/secured.jsp
~~~
    <!-- ログアウトリンク -->
    <a href="http://localhost:8080/realms/sample1/protocol/openid-connect/logout?post_logout_redirect_uri=http://localhost:8180/SampleWebApp/&amp;client_id=sample-web-app">Logout</a>

~~~

### マルチテナント対応

複数のRealmに1つのwarファイルで対応する。

2つのRealmを作成する。それぞれにClientを作成する。<br>
各RealmのClientに対応するoidc.jsonを作成する。

#### src/main/resources/oidc-sample1.json
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

#### src/main/resources/oidc-sample2.json
~~~
{
    "client-id" : "my-app-sample2",
    "provider-url" : "${env.OIDC_PROVIDER_URL:http://localhost:8080}/realms/sample2",
    "credentials" : {
        "secret" : "jhJrfbz6yU5dshWIZwdIurqI0efbv64W"
    },
    "principal-attribute" : "preferred_username",
    "ssl-required" : "EXTERNAL"
}
~~~

Webアプリケーションのコンテキストパスを判別して適切なoidc.jsonを返却するよう、ConfigResolverを作成する。

#### src/main/java/com/example/MyCustomConfigResolver.java
~~~
package com.example;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.wildfly.security.http.oidc.OidcClientConfiguration;
import org.wildfly.security.http.oidc.OidcClientConfigurationBuilder;
import org.wildfly.security.http.oidc.OidcClientConfigurationResolver;
import org.wildfly.security.http.oidc.OidcHttpFacade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyCustomConfigResolver implements OidcClientConfigurationResolver {

    private final Map<String, OidcClientConfiguration> cache = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(MyCustomConfigResolver.class);

    @Override
    public OidcClientConfiguration resolve(OidcHttpFacade.Request request) {
        String path = request.getURI();

        logger.info("path:" + path);

        // URLに含まれる文字列によってテナント設定を切り替える
        String tenant = "sample1";
        if (path.contains("/SampleWebApp2")) {
            tenant = "sample2";
        }
        logger.info("tenant:" + tenant);

        OidcClientConfiguration clientConfiguration = cache.get(tenant);
        if (clientConfiguration == null) {
            InputStream is = getClass().getResourceAsStream("/oidc-" + tenant + ".json");
            clientConfiguration = OidcClientConfigurationBuilder.build(is);
            cache.put(tenant, clientConfiguration);
        }
        return clientConfiguration;
    }
}
~~~

web.xmlに、MyCustomConfigResolver.javaを利用するよう設定を追加する。

~~~
    <context-param>
        <param-name>oidc.config.resolver</param-name>
        <param-value>com.example.MyCustomConfigResolver</param-value>
    </context-param>
~~~

pom.xmlに`profile`を追加する。プロファイルを指定し、ビルドする際にwarファイル名を変更する。

~~~
    <profiles>
        <profile>
            <id>sample1</id>
            <properties>
                <warName>SampleWebApp</warName>
            </properties>
        </profile>
        <profile>
            <id>sample2</id>
            <properties>
                <warName>SampleWebApp2</warName>
            </properties>
        </profile>
    </profiles>

    省略
    <build>
        <finalName>${warName}</finalName>
        <pluginManagement>
            <plugins>    
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-war-plugin</artifactId>
                    <version>${org.apache.maven.plugins.maven-war-plugin.version}</version>
                    <configuration>
                        <warName>${warName}</warName>
                        <archive>
                            <addMavenDescriptor>false</addMavenDescriptor>
                            <manifest>
                                <addDefaultImplementationEntries>true</addDefaultImplementationEntries>
                            </manifest>
                        </archive>
                    </configuration>
                </plugin>    
~~~

ビルドコマンド

~~~
mvn wildfly:deploy -P sample1
~~~

もしくは

~~~
mvn wildfly:deploy -P sample2
~~~

動作確認:

http://localhost:8180/SampleWebApp/secured

http://localhost:8180/SampleWebApp2/secured
