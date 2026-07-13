// Gold loyalty members get a discount on any order of $25 or more.
ctx.order.getLoyaltyTier() === 'GOLD' && ctx.order.getTotal() >= 25