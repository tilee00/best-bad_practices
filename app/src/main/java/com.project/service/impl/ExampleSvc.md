
> List<BrandEntity> brandsEntities = brandRepository.findByMerchantOrderByBrandNameAsc(entityManager.getReference(MerchantEntity.class, merchantId));
        