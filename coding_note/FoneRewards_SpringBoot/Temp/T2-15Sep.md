```java

    // Q1 why is @ParameterObject @SortDefault(value = "requestedAt", direction = Sort.Direction.ASC) used here?
    @MerchantActionLogged(
        action = ActionType.VIEW,
        module = TargetModule.MARKETING_TOOLS,
        remarks = "'View ' + #scope + ' campaigns approval list with status ' " +
                  "+ #status + ' for merchant ' + @securitySvc.getCurrentMerchantId()"
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ISSUER_MERCHANT') or hasAuthority('ISSUER_ADMIN')")
    public ResponseEntity<ApiResponse<CampaignApprovalList>>
    getApprovalListByMerchantIdAndStatus(@RequestParam ApprovalRequestStatus status,
                                         @RequestParam CampaignScope scope,
                                         @ParameterObject @SortDefault(value = "requestedAt", direction = Sort.Direction.ASC)
                                         Pageable pageable) {
        return campaignApprovalService.getApprovalListByMerchantIdAndStatus(securityService.getCurrentMerchantId(),
                status, scope, pageable)
            .toResponseEntity();
    }

```