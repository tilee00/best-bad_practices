```java
public enum CampaignScope {

    LOCAL,
    GLOBAL;

    @JsonCreator
    public static CampaignScope fromString(String text) {
        for (var scope : CampaignScope.values()) {
            if (scope.name().equalsIgnoreCase(text)) {
                return scope;
            }
        }
        return null;
    }
}
    // Q1
    // why #campaignScope can get value
    // why @securitySvc.getCurrentMerchantId() can get value

    @MerchantActionLogged(
        action = ActionType.VIEW,
        module = TargetModule.MARKETING_TOOLS,
        remarks = "'View ' + #campaignScope + ' campaigns overview for merchant ' + @securitySvc.getCurrentMerchantId()"
    )
    @GetMapping(value = "/overview", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<CampaignTypeOverviewItem>>> getCampaignTypeOverview(@RequestParam CampaignScope campaignScope) {
        return campaignOverviewService.getCampaignTypeOverview(securityService.getCurrentMerchantId(), campaignScope)
            .toResponseEntity();
    }

    //---

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<List<CampaignTypeOverviewItem>> getCampaignTypeOverview(Integer merchantId,
                                                                               CampaignScope campaignScope) {
        var campaignOverviewMap = campaignRepository.getCampaignOverviewByScope(campaignScope, merchantId)
            .stream()
            .collect(Collectors.toMap(CampaignOverviewProjection::getCampaignType, Function.identity(),
                (o, n) -> o));

        var resultList = Arrays.stream(CampaignType.values())
            // Q2
            // how this getDefaultOverview work?
            .map(it -> campaignOverviewMap.getOrDefault(it, getDefaultOverview(it)))
            .map(this::mapToOverviewItem)
            .collect(Collectors.toList());
        var inactiveCampaignCount = campaignRepository.countExpiredCampaignOverview(merchantId, campaignScope);
        resultList.add(CampaignTypeOverviewItem.builder()
            .typeName("Inactive")
            .typeCode(INACTIVE_TYPE_CODE)
            .description("Inactive campaigns which are waiting for approval")
            .inactiveCount(inactiveCampaignCount)
            .build());
        return ApiResponse.setSuccess(resultList);
    }

    //---

    private CampaignOverviewProjection getDefaultOverview(CampaignType campaignType) {
        return new CampaignOverviewProjection() {
            @Override
            public CampaignType getCampaignType() {
                return campaignType;
            }

            @Override
            public Integer getActiveCount() {
                return 0;
            }

            @Override
            public Integer getInactiveCount() {
                return 0;
            }
        };
    }

    //---
    
    // Q3
    // how this translateLabel work?
    private CampaignTypeOverviewItem mapToOverviewItem(CampaignOverviewProjection item) {
        return CampaignTypeOverviewItem.builder()
            .typeName(SelectableEnum.translateLabel(item.getCampaignType(), this::getI18nMessage))
            .typeCode(item.getCampaignType().getTypeCode())
            .description(getI18nMessage(item.getCampaignType().getDescription()))
            .activeCount(item.getActiveCount())
            .inactiveCount(item.getInactiveCount())
            .build();
    }

    @JsonIgnore
    static String translateLabel(
        SelectableEnum enumVal,
        UnaryOperator<String> i18nFunc
    ) {
        // Q4
        // what is this UnaryOperator when need to use?
        return Optional.ofNullable(enumVal)
            .map(SelectableEnum::getI18nCode)
            .map(i18nFunc)
            .orElse(null);
    }

    //---
    
@Getter
public enum CampaignType implements SelectableEnum {

    REFERRAL(false, true, CampaignRewardType.values().length,
        List.of(ReferralCampaignValidationGroup.class)),
    PROMOTION(false, true, CampaignRewardType.values().length, List.of()),
    PRODUCT_LAUNCH(false, true, CampaignRewardType.values().length, List.of()),
    GAMIFIED(true, true, 1, List.of());

    private final String typeCode;
    private final String typeName;
    private final String description;
    private final boolean isGamified;
    private final boolean showProductAssign;
    private final int maxRewardVariant;
    private final List<Class<?>> validationGroups;

    CampaignType(boolean isGamified,
                 boolean showProductAssign, int maxRewardVariant,
                 List<Class<?>> validationGroups) {
        this.typeCode = name().toLowerCase();
        this.typeName = "enum.campaign_type.name." + this.typeCode;
        this.description = "enum.campaign_type.description." + this.typeCode;
        this.isGamified = isGamified;
        this.showProductAssign = showProductAssign;
        this.maxRewardVariant = maxRewardVariant;
        this.validationGroups = validationGroups;
    }


    @JsonCreator
    public static CampaignType fromString(String val) {
        if (val == null || val.isBlank()) {
            return null;
        }

        return Stream.of(CampaignType.values())
                .filter(e -> e.typeCode.equalsIgnoreCase(val))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getSelectionKey() {
        return this.typeCode;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getI18nCode() {
        return this.typeName;
    }

    @Override
    public String getSelectionValue() {
        return this.typeName;
    }

    public Class<?>[] getArrayValidationGroups() {
        return validationGroups.toArray(new Class<?>[0]);
    }
}


```