# Smart E-Commerce Order & Inventory Backend

### Issue Try to Solve
- race conditions (multiple users buying the last item),
- unauthorized access (data leaks),
- slow performance (heavy operations blocking the system),
- messy logs (difficulty finding bugs)

### Feature
- 

### Note START below

---

### Core Applications of Custom Annotations & AOP

Each custom annotation targets a specific real-world problem using Aspect-Oriented Programming (AOP):

1. **`@RateLimit`**
* **Problem:** Spam attacks or bot traffic overloading critical endpoints (e.g., flash sales).
* **AOP Action:** Intercepts the request before it reaches the controller, checks the user's request count in Redis, and blocks the request with a **429 Too Many Requests** error if the threshold is exceeded.


2. **`@DistributedLock`**
* **Problem:** Race conditions when updating stock during simultaneous purchases (double-selling stock).
* **AOP Action:** Acquires a Redis-based distributed lock using the item ID before executing the method, ensuring only one thread updates the inventory at a time, then releases the lock automatically.


3. **`@RequireRole`**
* **Problem:** Hardcoding permission checks inside business logic clutters the code.
* **AOP Action:** Inspects the request context or token header before method execution; if the user lacks the required role (e.g., `ADMIN`), it throws an **Access Denied** exception.


4. **`@ExecutionTimeLog`**
* **Problem:** Identifying slow database queries or slow external API calls without modifying method signatures.
* **AOP Action:** Wraps the target method, starts a timer before execution, stops it after execution, and logs the total duration (in milliseconds) if it exceeds a set threshold.


5. **`@AuditLog`**
* **Problem:** Tracking critical business operations (e.g., price updates, order cancellations) for compliance and debugging.
* **AOP Action:** Captures input arguments, current user ID, and method results after execution, then writes an immutable audit record to the database asynchronously.


6. **`@AsyncRetry`**
* **Problem:** Temporary network glitches causing failures when calling external systems (e.g., payment gateways).
* **AOP Action:** Catches specific exceptions and automatically retries the method execution up to a configured maximum count with exponential backoff before failing.



---

### Endpoints Blueprint (5 Fully Functional CRUD Endpoints)

#### 1. POST `/api/v1/products` (Create Product)

* **Purpose:** Allows administrators to register new products in the catalog.
* **Features Used:**
* Base Spring Data JPA CRUD (`save`)
* **`@RequireRole(Role.ADMIN)`**: Restricts product creation to administrators only.
* **`@AuditLog(action = "CREATE_PRODUCT")`**: Logs who created the product and the initial stock data.


#### 2. GET `/api/v1/products/{id}` (Get Product Details)

* **Purpose:** Retrieves product details and current inventory levels for buyers.
* **Features Used:**
* Base Spring Data JPA CRUD (`findById`)
* **`@RateLimit(maxRequests = 50, windowSeconds = 60)`**: Protects public product endpoints from scraper bots.
* **`@ExecutionTimeLog(thresholdMs = 200)`**: Warns developers if fetching product details becomes too slow due to missing database indexes.


#### 3. PUT `/api/v1/products/{id}/stock` (Update Stock Level)

* **Purpose:** Updates existing inventory counts directly for restocks or physical audits.
* **Features Used:**
* Base Spring Data JPA CRUD (`findById`, `save`)
* **`@RequireRole(Role.ADMIN)`**: Ensures only warehouse managers or admins alter inventory balances.
* **`@DistributedLock(key = "#id")`**: Prevents concurrent manual updates from overwriting each other.
* **`@AuditLog(action = "UPDATE_STOCK")`**: Keeps a strict audit trail of stock adjustments.


#### 4. POST `/api/v1/orders` (Checkout / Place Order)

* **Purpose:** Handles customer purchases, deducts stock, and triggers downstream operations.
* **Features Used:**
* Base Spring Data JPA CRUD (`save` for Order, `save` for Order Items)
* **`@RateLimit(maxRequests = 5, windowSeconds = 10)`**: Prevents duplicate order submissions from accidental rapid clicks.
* **`@DistributedLock(key = "#request.productId")`**: Locks the specific product during checkout so stock deduction is thread-safe.
* **`@AsyncRetry(maxAttempts = 3)`**: Retries internal inventory allocation calls if transient database locks occur.

#### 5. DELETE `/api/v1/orders/{id}` (Cancel Order)

* **Purpose:** Cancels an unpaid or pending order and restores the deducted inventory stock back to the catalog.
* **Features Used:**
* Base Spring Data JPA CRUD (`findById`, `delete` / status update)
* **`@DistributedLock(key = "#order.productId")`**: Safely restores stock levels without race conditions.
* **`@AuditLog(action = "CANCEL_ORDER")`**: Records cancellation reason, user ID, and timestamp for customer service tracking.

---