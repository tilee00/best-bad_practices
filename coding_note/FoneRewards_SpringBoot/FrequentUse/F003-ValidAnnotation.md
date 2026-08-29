> @Valid 
- validate the payload send whether is follow the rule set in request bean

# When need to use?
- when annotation rule set at the request bean 

# When NOT need to use?
- when NO annotation rule set at the request bean

# Example code

- usage
```java

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@LogRequestResponse
public class DashboardController extends BaseController{

    private final IDashboardService service;

    @PostMapping("/onboarding/complete")
    public ApiResponse<String> complete(@RequestBody @Valid OnboardingReq req) {
        service.onboardingComplete(req.getOnboardingCode());
        return ApiResponse.setSuccess("Onboarding completed successfully");
    }
}


@Data
public class OnboardingReq {
    @NotBlank
    private String onboardingCode;
}
```

# Example payload & response
```json
// payload
{
  "onboardingCode": ""
}

// response
{
  "responseCode": "CE-400",
  "message": "Invalid input data. Please check the field error list.",
  "data": null,
  "fieldErrors": [
    {
      "objectName": "onboardingReq",
      "field": "onboardingCode",
      "message": "Onboarding Code Is required"
    }
  ]
}
```

# Normal Example validation annotation
- use in request bean
```java

// date / time
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateTo;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime timeFrom;

// min / max
    @Min(0)
    private int page;
    @Min(1)
    private int size;
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
    @Size(max = 500, message = "Value must not exceed 500 characters")
    private String value;

// mandatory
    @NotBlank(message = "Phone number must not be blank")
    private String phoneNumber;
    @NotNull(message = "OTP type must not be null")
    private OtpTypeEnum otpType;

// custom JSON key
    // with @JsonProperty [expects in format "message_id": 123]
    // NO @JsonProperty [expects in format "messageId": 123]
    @JsonProperty("message_id")
    private String messageId;

```

# Special Example validation annotation
1. only apply validation when the request come from custom group
```java

// empty interface as group
    public interface AdminRequiredMerchantIdValidationGroup {
    }

// both admin and normal user use this request bean; only apply the validation when in admin group
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class OutletJoinRewardUpdateReq {
        @NotNull(groups = {AdminRequiredMerchantIdValidationGroup.class})
        private Integer merchantId;
    }

// Admin controller
    public ResponseEntity<ApiResponse<List<OutletJoinRewardFormUpdateRes>>>
    updateConfigs(@Validated({Default.class, AdminRequiredMerchantIdValidationGroup.class}) 
                  @RequestBody OutletJoinRewardUpdateReq payload) {
        return joinRewardCampaignUpdateSvc.updateConfig(payload)
            .toResponseEntity();
    }

// Normal user controller
    public ResponseEntity<ApiResponse<List<OutletJoinRewardFormUpdateRes>>>
    updateConfigs(@Valid @RequestBody OutletJoinRewardUpdateReq payload) {
        payload.setMerchantId(securityService.getCurrentMerchantId());
        return joinRewardCampaignUpdateSvc.updateConfig(payload)
            .toResponseEntity();
    }

```

