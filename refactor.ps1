$ErrorActionPreference = 'Stop'

$base = "src\main\java\nguyennhatquan\springbootreview"

Write-Host "Creating directories..."
New-Item -ItemType Directory -Force -Path "$base\shared" | Out-Null
New-Item -ItemType Directory -Force -Path "$base\shared\dto" | Out-Null
New-Item -ItemType Directory -Force -Path "$base\shared\service" | Out-Null
New-Item -ItemType Directory -Force -Path "$base\user\controller" | Out-Null
New-Item -ItemType Directory -Force -Path "$base\user\service" | Out-Null
New-Item -ItemType Directory -Force -Path "$base\user\dto" | Out-Null
New-Item -ItemType Directory -Force -Path "$base\product\controller" | Out-Null
New-Item -ItemType Directory -Force -Path "$base\product\service" | Out-Null
New-Item -ItemType Directory -Force -Path "$base\product\dto\product" | Out-Null
New-Item -ItemType Directory -Force -Path "$base\order\controller" | Out-Null
New-Item -ItemType Directory -Force -Path "$base\order\service" | Out-Null
New-Item -ItemType Directory -Force -Path "$base\order\dto" | Out-Null
New-Item -ItemType Directory -Force -Path "$base\order\util" | Out-Null

Write-Host "Moving shared files..."
Move-Item -Path "$base\entity" -Destination "$base\shared\" -Force
Move-Item -Path "$base\repository" -Destination "$base\shared\" -Force
Move-Item -Path "$base\config" -Destination "$base\shared\" -Force
Move-Item -Path "$base\security" -Destination "$base\shared\" -Force
Move-Item -Path "$base\interceptor" -Destination "$base\shared\" -Force
Move-Item -Path "$base\exception" -Destination "$base\shared\" -Force
Move-Item -Path "$base\service\cache" -Destination "$base\shared\service\" -Force
Move-Item -Path "$base\dto\common" -Destination "$base\shared\dto\" -Force
Move-Item -Path "$base\dto\CacheSyncEvent.java" -Destination "$base\shared\dto\" -Force

Write-Host "Moving user files..."
Move-Item -Path "$base\controller\AuthController.java" -Destination "$base\user\controller\" -Force
Move-Item -Path "$base\service\UserService.java" -Destination "$base\user\service\" -Force
Move-Item -Path "$base\dto\auth" -Destination "$base\user\dto\" -Force

Write-Host "Moving product files..."
Move-Item -Path "$base\controller\ProductController.java" -Destination "$base\product\controller\" -Force
Move-Item -Path "$base\controller\CategoryController.java" -Destination "$base\product\controller\" -Force
Move-Item -Path "$base\service\ProductService.java" -Destination "$base\product\service\" -Force
Move-Item -Path "$base\service\CategoryService.java" -Destination "$base\product\service\" -Force
Get-ChildItem -Path "$base\dto\Product" -File | Move-Item -Destination "$base\product\dto\product\" -Force
Remove-Item -Path "$base\dto\Product" -Force
Move-Item -Path "$base\dto\category" -Destination "$base\product\dto\" -Force

Write-Host "Moving order files..."
Move-Item -Path "$base\controller\OrderController.java" -Destination "$base\order\controller\" -Force
Move-Item -Path "$base\controller\CartController.java" -Destination "$base\order\controller\" -Force
Move-Item -Path "$base\controller\CheckoutController.java" -Destination "$base\order\controller\" -Force
Move-Item -Path "$base\controller\MomoPaymentController.java" -Destination "$base\order\controller\" -Force
Move-Item -Path "$base\service\OrderService.java" -Destination "$base\order\service\" -Force
Move-Item -Path "$base\service\CartService.java" -Destination "$base\order\service\" -Force
Move-Item -Path "$base\service\CheckoutService.java" -Destination "$base\order\service\" -Force
Move-Item -Path "$base\service\MomoPaymentService.java" -Destination "$base\order\service\" -Force
Move-Item -Path "$base\dto\order" -Destination "$base\order\dto\" -Force
Move-Item -Path "$base\dto\cart" -Destination "$base\order\dto\" -Force
Move-Item -Path "$base\dto\momo" -Destination "$base\order\dto\" -Force
Move-Item -Path "$base\util\MomoSignatureUtil.java" -Destination "$base\order\util\" -Force

Write-Host "Cleaning up empty directories..."
Remove-Item -Path "$base\controller" -Force -Recurse -ErrorAction SilentlyContinue
Remove-Item -Path "$base\service" -Force -Recurse -ErrorAction SilentlyContinue
Remove-Item -Path "$base\dto" -Force -Recurse -ErrorAction SilentlyContinue
Remove-Item -Path "$base\util" -Force -Recurse -ErrorAction SilentlyContinue
Remove-Item -Path "$base\SpringBootReviewApplication.java" -Force -ErrorAction SilentlyContinue

Write-Host "Defining replacement patterns..."
$replacements = @{
    "nguyennhatquan\.springbootreview\.entity" = "nguyennhatquan.springbootreview.shared.entity"
    "nguyennhatquan\.springbootreview\.repository" = "nguyennhatquan.springbootreview.shared.repository"
    "nguyennhatquan\.springbootreview\.config" = "nguyennhatquan.springbootreview.shared.config"
    "nguyennhatquan\.springbootreview\.security" = "nguyennhatquan.springbootreview.shared.security"
    "nguyennhatquan\.springbootreview\.interceptor" = "nguyennhatquan.springbootreview.shared.interceptor"
    "nguyennhatquan\.springbootreview\.exception" = "nguyennhatquan.springbootreview.shared.exception"
    "nguyennhatquan\.springbootreview\.service\.cache" = "nguyennhatquan.springbootreview.shared.service.cache"
    "nguyennhatquan\.springbootreview\.dto\.common" = "nguyennhatquan.springbootreview.shared.dto.common"
    "nguyennhatquan\.springbootreview\.dto\.CacheSyncEvent" = "nguyennhatquan.springbootreview.shared.dto.CacheSyncEvent"

    "nguyennhatquan\.springbootreview\.controller\.AuthController" = "nguyennhatquan.springbootreview.user.controller.AuthController"
    "nguyennhatquan\.springbootreview\.service\.UserService" = "nguyennhatquan.springbootreview.user.service.UserService"
    "nguyennhatquan\.springbootreview\.dto\.auth" = "nguyennhatquan.springbootreview.user.dto.auth"

    "nguyennhatquan\.springbootreview\.controller\.ProductController" = "nguyennhatquan.springbootreview.product.controller.ProductController"
    "nguyennhatquan\.springbootreview\.controller\.CategoryController" = "nguyennhatquan.springbootreview.product.controller.CategoryController"
    "nguyennhatquan\.springbootreview\.service\.ProductService" = "nguyennhatquan.springbootreview.product.service.ProductService"
    "nguyennhatquan\.springbootreview\.service\.CategoryService" = "nguyennhatquan.springbootreview.product.service.CategoryService"
    "nguyennhatquan\.springbootreview\.dto\.Product" = "nguyennhatquan.springbootreview.product.dto.product"
    "nguyennhatquan\.springbootreview\.dto\.category" = "nguyennhatquan.springbootreview.product.dto.category"

    "nguyennhatquan\.springbootreview\.controller\.OrderController" = "nguyennhatquan.springbootreview.order.controller.OrderController"
    "nguyennhatquan\.springbootreview\.controller\.CartController" = "nguyennhatquan.springbootreview.order.controller.CartController"
    "nguyennhatquan\.springbootreview\.controller\.CheckoutController" = "nguyennhatquan.springbootreview.order.controller.CheckoutController"
    "nguyennhatquan\.springbootreview\.controller\.MomoPaymentController" = "nguyennhatquan.springbootreview.order.controller.MomoPaymentController"
    "nguyennhatquan\.springbootreview\.service\.OrderService" = "nguyennhatquan.springbootreview.order.service.OrderService"
    "nguyennhatquan\.springbootreview\.service\.CartService" = "nguyennhatquan.springbootreview.order.service.CartService"
    "nguyennhatquan\.springbootreview\.service\.CheckoutService" = "nguyennhatquan.springbootreview.order.service.CheckoutService"
    "nguyennhatquan\.springbootreview\.service\.MomoPaymentService" = "nguyennhatquan.springbootreview.order.service.MomoPaymentService"
    "nguyennhatquan\.springbootreview\.dto\.order" = "nguyennhatquan.springbootreview.order.dto.order"
    "nguyennhatquan\.springbootreview\.dto\.cart" = "nguyennhatquan.springbootreview.order.dto.cart"
    "nguyennhatquan\.springbootreview\.dto\.momo" = "nguyennhatquan.springbootreview.order.dto.momo"
    "nguyennhatquan\.springbootreview\.util\.MomoSignatureUtil" = "nguyennhatquan.springbootreview.order.util.MomoSignatureUtil"
}

Write-Host "Updating package and import declarations..."
$files = Get-ChildItem -Path "$base" -Recurse -Filter "*.java"
foreach ($file in $files) {
    if ($file.Extension -eq ".java") {
        $content = Get-Content -Path $file.FullName -Raw
        $modified = $false
        foreach ($key in $replacements.Keys) {
            if ($content -match $key) {
                $content = $content -replace $key, $replacements[$key]
                $modified = $true
            }
        }
        if ($modified) {
            Set-Content -Path $file.FullName -Value $content -NoNewline
        }
    }
}
Write-Host "Refactoring complete."
