# Code Design

### traditional way
```java
// 1. The Data Item (What we are storing)
class Campaign {
    String name;
    
    public Campaign(String name) {
        this.name = name;
    }
}

// 2. The Repository
interface CampaignRepository {
    void save(Campaign campaign);
}

class InMemoryCampaignRepository implements CampaignRepository {
    // A simple Java Map acts as our short-term memory box (RAM)
    private java.util.Map<String, Campaign> storage = new java.util.HashMap<>();

    @Override
    public void save(Campaign campaign) {
        storage.put(campaign.name, campaign);
        System.out.println("[Storage Box] Successfully saved campaign: " + campaign.name);
    }
}

// 3. The Service
interface CampaignService {
    void createCampaign(String name);
}

class DefaultCampaignService implements CampaignService {
    private CampaignRepository repository; // The manager's storage box

    // This is Dependency Injection: We hand the storage box to the manager here
    public DefaultCampaignService(CampaignRepository repository) {
        this.repository = repository;
    }

    @Override
    public void createCampaign(String name) {
        System.out.println("[Manager] Starting to create campaign: " + name);
        Campaign newCampaign = new Campaign(name);
        
        // The manager uses the storage box to save the work
        repository.save(newCampaign);
    }
}

// 4. Running the Program
public class Main {
    public static void main(String[] args) {
        // Interface helperName = new BusinessRuleService(new dataSavingTools());
        CampaignService campaignService = new DefaultCampaignService(new InMemoryCampaignRepository());

        // Now we tell the manager to do their job
        campaignService.createCampaign("Summer Super Sale");
    }
}
```

### modern way, no database connection
```java
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.HashMap;

// 1. The Data Item (Modern Java Record)
public record Campaign(String name) {}

// 2. The Repository 
public interface CampaignRepository {
    void save(Campaign campaign);
}

@Repository // Spring manages this data storage box automatically
class InMemoryCampaignRepository implements CampaignRepository {
    private final Map<String, Campaign> storage = new HashMap<>();

    @Override
    public void save(Campaign campaign) {
        storage.put(campaign.name, campaign);
        System.out.println("[Storage Box] Successfully saved campaign: " + campaign.name);
    }
}

// 3. The Service
public interface CampaignService {
    void createCampaign(String name);
}

@Service // Spring manages this manager automatically
class DefaultCampaignService implements CampaignService {
    private final CampaignRepository repository;

    public DefaultCampaignService(CampaignRepository repository) {
        this.repository = repository;
    }

    @Override
    public void createCampaign(String name) {
        System.out.println("[Manager] Starting to create campaign: " + name);
        Campaign newCampaign = new Campaign(name);
        repository.save(newCampaign);
    }
}

/**
 * never see new DefaultCampaignService(new InMemoryCampaignRepository()) 
 * floating around in modern business code
 * Spring Boot starts up, looks at annotations like @Service and @Repository, 
 * and wires everything together for you automatically.
**/

```

### modern way, with database connection
```java
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ==========================================
// 1. THE DATABASE ENTITY (Mapping to a real SQL table)
// ==========================================
@Entity
@Table(name = "campaigns")
public class CampaignEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Every row in a real database needs a unique ID number

    @Column(nullable = false)
    private String name;

    // Constructors (Required by JPA)
    public CampaignEntity() {}

    public CampaignEntity(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

// ==========================================
// 2. THE REPOSITORY (Spring Data JPA magic)
// ==========================================
// By extending JpaRepository, Spring automatically writes all the SQL save/find code for you!
@Repository
public interface CampaignRepository extends JpaRepository<CampaignEntity, Long> {
    // No need to write HashMap code here. Spring handles it out-of-the-box.
}

// ==========================================
// 3. THE SERVICE (The business manager)
// ==========================================
public interface CampaignService {
    void createCampaign(String name);
}

@Service
class DefaultCampaignService implements CampaignService {

    private final CampaignRepository repository;

    // Modern constructor injection (Spring automatically hands the database repository here)
    public DefaultCampaignService(CampaignRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional // Ensures database changes are safely saved, or rolled back if an error happens
    public void createCampaign(String name) {
        System.out.println("[Manager] Saving campaign to the real database: " + name);
        
        // Create the database model object
        CampaignEntity entity = new CampaignEntity(name);
        
        // Save it straight to the real database using Spring Data JPA
        repository.save(entity);
    }
}
```