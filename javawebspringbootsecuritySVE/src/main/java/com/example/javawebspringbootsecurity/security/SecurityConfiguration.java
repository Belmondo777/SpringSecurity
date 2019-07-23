package com.example.javawebspringbootsecurity.security;
import com.example.javawebspringbootsecurity.security.AuthenticationFilter;
import com.example.javawebspringbootsecurity.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration //novi tip komponente u Springu
@EnableWebSecurity //ukljucujemo security modul
@EnableGlobalMethodSecurity(prePostEnabled = true) // da bi anotacije @PreAuthorize nad metodama radile
//na metode koje se misle, na rutama u kontroleru... Ukljcujemo nam ovo preAuthorized
public class SecurityConfiguration extends WebSecurityConfigurerAdapter { //pratimo sablon koje nam daje websecutiry daje
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    public void configureAuthentication( //deo koji govori da cemo mi koristiti UserdetailsService, sta koristimo za UserdetailsSerivce i koji paswordEncoder koristimo
            AuthenticationManagerBuilder authenticationManagerBuilder)
            throws Exception {

        authenticationManagerBuilder
                .userDetailsService(this.userDetailsService).passwordEncoder( //pretvara sifru iz teksta u nesto sto je sifrovano, OBAVEZNO!... MORA DA SE KORISTI!!!
                passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public AuthenticationFilter authenticationTokenFilterBean() //govori koji cemo da koristimo Authentification filter
            throws Exception {
        AuthenticationFilter authenticationTokenFilter = new AuthenticationFilter();
        authenticationTokenFilter
                .setAuthenticationManager(authenticationManagerBean());
        return authenticationTokenFilter;
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception { //bitan deo... trazi da hhtp security privezes sve zivo kako ce on da filtira neki zahtev
        httpSecurity
                .authorizeRequests()
                .filterSecurityInterceptorOncePerRequest(false)
                .antMatchers("/login").permitAll()
                .antMatchers("/register").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                    .loginProcessingUrl("/login")
                    .successForwardUrl("/index")
                    .failureForwardUrl("/bad_login")
                .permitAll();
//                .csrf().disable() // csrf -> zastite forme... Posalje zahtev kao sifrovan deo iz forme...
//                .sessionManagement() //manager sesije
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // polisa kreiranja sesije... Najcesce je stateless, ne menja se skoro nikad
//                .and() //vezuje sledeci zahtev
//                .authorizeRequests()
//                .anyRequest().authenticated()
//                .antMatchers("/login").permitAll();
//                .and().formLogin()
//                .loginPage("/login").permitAll();
//                .antMatchers("/login").anonymous()// bez logovanja dostupni samo servisi login
////              .antMatchers("/**").permitAll() ---> Za koje rute stavljamo neke dozvole, i koje rute mogu da budu ulogovane i ostalo...
//                .anyRequest().permitAll(); // za ostale akcije se mora biti ulogovan

        // pre standardnog Spring filtera, postavlja se nas filter za postavljanje korisnika na osnovu
        // JWT tokena u zahtevu
        httpSecurity.addFilterBefore(authenticationTokenFilterBean(),
                UsernamePasswordAuthenticationFilter.class);
    }
}
