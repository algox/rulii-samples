// Summer Sale: orders of $50+ placed June through August.
// "ctx" exposes the rulii Bindings; Java getters are called directly.
ctx.order.getTotal() >= 50 && ctx.orderMonth >= 6 && ctx.orderMonth <= 8