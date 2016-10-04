package au.org.aekos.service.auth;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AekosUserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(AekosUserDetailsService.class);

	@Autowired
	private AuthStorageService authStorageService;
	
    /**
	 * 
	 */
	public AekosUserDetailsService() {
		super();
        logger.debug(" AekosUserDetailsService created..");
	}


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug(" loadUserByUsername called with: " + username);
        try {
        	AekosApiAuthKey key = new AekosApiAuthKey(username);
            boolean exists = authStorageService.exists(key);
            if (!exists) {
                logger.warn("username not found (" + username + ")");
                return null;
            }
            logger.debug(" user from username " + username);
            return new org.springframework.security.core.userdetails.User(username, "", getAuthorities(username));
        }
        catch (Exception e) {
            throw new UsernameNotFoundException("User not found");
        }
    }
    
	public Account findByUsername(String username) {
        logger.debug(" findByUsername called with: " + username);
        try {
        	AekosApiAuthKey key = new AekosApiAuthKey(username);
            boolean exists = authStorageService.exists(key);
            if (!exists) {
                logger.warn("username not found (" + username + ")");
                return null;
            }
            logger.debug(" user from username " + username);
            return new Account(username, "");
        }
        catch (Exception e) {
            throw new UsernameNotFoundException("User not found");
        }
   	
		
	}


	public boolean userExists(String username) {
		if (loadUserByUsername(username) != null) {
			return (true);
		}
		return (false);
	}
	
    private Set<GrantedAuthority> getAuthorities(String user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        // Default to a normal USER
        // TODO - add admin user role
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("USER");
        authorities.add(grantedAuthority);
        logger.debug("user authorities are " + authorities.toString());
        return authorities;
    }


    
}
