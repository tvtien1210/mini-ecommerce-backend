// PAGE LOAD

const productContainerElement = document.getElementById("productContainer");
const searchKeywordElement = document.getElementById("search-keyword");
const searchButtonElement = document.getElementById("search-button");
// Lấy thẻ HTML dùng để hiển thị pagination
const paginationElement = document.getElementById("pagination");
// Search khi click vao button elemnet cua filter keyword
const searchFilterKeyWordElement = document.getElementById("search-filter-keyword");

// Chờ HTML load xong
document.addEventListener("DOMContentLoaded", function () {

    //Intial Load : load ban dau, nen khong can load by search-keyword
    loadProducts();

    setupPaginationEvents();

    // Đăng ký Event Listener cho các nút Add to Cart
    setupAddToCartEvents();

});

// "".isBlank()=true, gia tri ban dau khi loadProducts la : (page = 0, size = 10, keyword="")
async function loadProducts( page = 0, keyword = "", size = 10){

    try {

        // URL mặc định
        let url =`/api/products?page=${page}&size=${size}`;

        // Nếu có keyword thì thêm keyword
        if (keyword) {
            url += `&keyword=${encodeURIComponent(keyword)}`;
        }

        const response = await apiFetch(url);

        if (!response) {
            return;
        }

        if (!response.ok) {
            throw new Error("Failed to load products");
        }

        // Backend trả về ProductPageDTO
        const productPage =
            await response.json();

        // Lấy danh sách Product của page hiện tại
        const products =
            productPage.products;

        renderProducts(products);

        renderPagination(
            productPage.currentPage,
            productPage.totalPages
        );

    } catch (error) {

        console.error("Failed:", error);
    }
}

function renderProducts(products){

    productContainerElement.innerHTML="";

    if(products.length==0){
        return `
            <div>Not found products</div>
        `
    }


    products.forEach(function (product){

        productContainerElement.innerHTML += `

            <div class="col-12 col-sm-6 col-lg-4">

                        <!-- PRODUCT CARD -->

                        <div class="card
                                product-card
                                text-center
                                p-4
                                h-100
                                d-flex
                                flex-column"
                                data-product-id="${product.id}">


                            <!-- =========================================
                                 PRODUCT IMAGE
                            ========================================== -->

                            <div class="product-image mb-3">

                                <img
                                        src="${product.imageUrl}"
                                        class="img-fluid"
                                        alt="${product.name}">

                            </div>


                            <!-- =========================================
                                 PRODUCT INFORMATION
                            ========================================== -->

                            <div class="product-info
                                    d-flex
                                    flex-column
                                    flex-grow-1">


                                <!-- PRODUCT NAME -->

                                <h5 class="product-title text-light">

                                    ${product.name}

                                </h5>


                                <!-- PRODUCT DESCRIPTION -->

                                <p class="product-description text-secondary">

                                    ${product.description}

                                </p>


                                <!-- PRODUCT PRICE -->

                                <p class="product-price
                                      text-primary
                                      fw-bold">

                                    ${product.price}

                                </p>


                                <!-- PRODUCT STOCK -->

                                <p class="text-secondary small">

                                    Stock:

                                    <span class="text-light">

                                    ${product.stock}

                                </span>

                                </p>


                                <!-- =====================================
                                     ADD TO CART
                                     mt-auto = push button to bottom
                                ====================================== -->

                                <button
                                        type="button"
                                        class="btn btn-primary w-100 mt-auto add-to-cart-btn"
                                        data-product-id="${product.id}">

                                    <i class="bi bi-cart-plus me-2"></i>

                                    Add to Cart

                                </button>


                            </div>

                        </div>

                    </div>


        `

    })

}

//RENDER PAGINATION

function renderPagination(
    currentPage, // Trang hiện tại, bắt đầu từ 0
    totalPages   // Tổng số trang
) {

    // Xóa pagination cũ trước khi render lại
    paginationElement.innerHTML = "";

    // Nếu chỉ có 1 trang hoặc không có trang nào
    // thì không cần hiển thị pagination
    if (totalPages <= 1) {
        return;
    }


    // ==================================================
    // Previous button
    // ==================================================

    paginationElement.innerHTML += `
        <li class="page-item
            ${currentPage === 0 ? "disabled" : ""}">

            <button
                class="page-link"
                data-page="${currentPage - 1}">

                Previous

            </button>

        </li>
    `;

    // Nếu đang ở trang đầu tiên (page = 0)
    // thì Previous sẽ bị disabled
    //
    // Ví dụ:
    // currentPage = 0
    // → data-page = -1
    // → nhưng button bị disabled nên không thể click


    // ==================================================
    // Page numbers
    // ==================================================

    // Duyệt qua tất cả các trang
    //
    // page = 0 → trang 1
    // page = 1 → trang 2
    // page = 2 → trang 3
    //
    // Backend cũng sử dụng page bắt đầu từ 0
    for (
        let page = 0;
        page < totalPages;
        page++
    ) {

        paginationElement.innerHTML += `

            <li class="page-item
                ${page === currentPage ? "active" : ""}">

                <button
                    class="page-link"
                    data-page="${page}">

                    ${page + 1}

                </button>

            </li>
        `;
    }

    // page dùng giá trị bắt đầu từ 0
    // nhưng người dùng nhìn thấy số bắt đầu từ 1
    //
    // page = 0 → hiển thị "1"
    // page = 1 → hiển thị "2"
    // page = 2 → hiển thị "3"


    // ==================================================
    // Next button
    // ==================================================

    paginationElement.innerHTML += `

        <li class="page-item
            ${currentPage === totalPages - 1
                ? "disabled"
                : ""}">

            <button
                class="page-link"
                data-page="${currentPage + 1}">

                Next

            </button>

        </li>
    `;

    // Nếu đang ở trang cuối:
    //
    // currentPage === totalPages - 1
    //
    // thì Next sẽ bị disabled.
}

//SEARCH BUTTON EVENT

searchButtonElement.addEventListener("click",function(){

    //trim() xử lý khoảng trắng đầu/cuối, còn encodeURIComponent() sẽ xử lý cả khoảng trắng ở giữa khi đưa vào URL.
    const keyword = searchKeywordElement.value.trim();

    loadProducts(0,keyword);

});


// PANIGATION EVENTS
function setupPaginationEvents(){

    paginationElement.addEventListener("click",function(event){

        const button = event.target.closest(".page-link");

        if(!button){return;}

        const page = Number(button.dataset.page);

        const keyword = searchKeywordElement.value.trim();

        if(page<0){return;}

        loadProducts(page,keyword)

    })

}
// ADD TO CART EVENTS

// Đăng ký sự kiện click cho các nút Add to Cart
function setupAddToCartEvents() {

    // Lấy tất cả button có class "add-to-cart-btn"
    const addToCartButtons =
        document.querySelectorAll(".add-to-cart-btn");


    // Duyệt qua từng button
    addToCartButtons.forEach(function (button) {

        // Đăng ký sự kiện click
        button.addEventListener("click", function () {


            const productId =
                Number(button.dataset.productId);


            // Mặc định khi User click
            // sẽ thêm 1 sản phẩm vào Cart
            const quantity = 1;


            // Gọi function addToCart()
            addToCart(
                productId,
                quantity
            );

        });

    });

}


// ADD TO CART

// Gọi Backend API để thêm Product vào Cart
async function addToCart(
    productId,
    quantity
) {

    try {

        // Kiểm tra dữ liệu trước khi gọi API
        console.log(
            "Adding product to cart:",
            productId,
            "quantity:",
            quantity
        );


        // CALL BACKEND API

        const response =
            await apiFetch(
                "/api/cart/items",
                {

                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({
                        productId: productId,
                        quantity: quantity
                    })

                }
            );


        // Nếu apiFetch không trả response
        // thì dừng
        if (!response) {
            return;
        }

        if(response.status === 401){
            window.location.href="/login";
        }


        // CHECK RESPONSE

        // Kiểm tra HTTP status
        // Ví dụ:
        // 200 / 201
        // response.ok = true
        // 400 / 401 / 403 / 500
        // response.ok = false
        if (!response.ok) {

            const errorData = await response.json();

            console.error("Add to cart failed",errorData);

            showToast(errorData.message, "warning");

            return;

        }


        // ==================================================
        // GET UPDATED CART
        // ==================================================

        // Backend trả về CartDTO
        //
        // Ví dụ:
        //
        // {
        //     id: 1,
        //     cartItems: [...],
        //     totalPrice: 50000000,
        //     currency: "VND"
        // }
        const cart =
            await response.json();


        console.log(
            "Cart after adding product:",
            cart
        );


        // Cập nhật số trên Cart Badge
        updateCartBadge(cart);


        // ==================================================
        // SUCCESS
        // ==================================================

        // Tạm thời chỉ thông báo thành công
        console.log(
            "Product added to cart successfully"
        );


        showToast(
            "Product added to cart!","success"
        );


    } catch (error) {

        // Nếu API lỗi,mat ket noi internet, function loi
        // hiển thị lỗi trong Console
        console.error(error);

        //Thong bao ra man hinh
        showToast(
            "Something went wrong. Please try again.","warning"
        );



    }

}


//TU DONG DIEN SEARCH KEYWORD

document.querySelectorAll(".search-filter-keyword").forEach(function(button) {

    button.addEventListener("click", function(){

    const keyword = button.dataset.keyword;

    loadProducts(0,keyword);

    });

});

//// Lấy element <input> dùng để nhập từ khóa tìm kiếm
//const searchKeywordElement =
//    document.getElementById("search-keyword");
//
//// Lấy tất cả các button có class="search-keyword"
//// Sau đó duyệt qua từng button
//document.querySelectorAll(".search-keyword").forEach(button => {
//
//    // Gắn sự kiện click cho từng button
//    button.addEventListener("click", () => {
//
//        // Lấy giá trị của thuộc tính data-keyword
//        // Ví dụ: data-keyword="iPhone" → keyword = "iPhone"
//        const keyword = button.dataset.keyword;
//
//        // Gán keyword vào ô input tìm kiếm
//        // Ví dụ: input.value = "iPhone"
//        searchKeywordElement.value = keyword;
//    });
//});


//FUNCTION SHOW TOAST

//Hien thi Toast voi noi dung message duoc truyen vao

function showToast(message, type = "success") {

    // Lấy Toast element
    const toastElement =
        document.getElementById("toast");

    // Lấy element chứa nội dung message
    const toastMessageElement =
        document.getElementById("toastMessage");


    // Xóa màu cũ
    toastElement.classList.remove(
        "text-bg-success",
        "text-bg-danger",
        "text-bg-warning"
    );


    // Thêm màu tương ứng với loại message
    if (type === "success") {

        toastElement.classList.add(
            "text-bg-success"
        );

    } else if (type === "danger") {

        toastElement.classList.add(
            "text-bg-danger"
        );

    } else if (type === "warning") {

        toastElement.classList.add(
            "text-bg-warning"
        );
    }


    // Đưa message vào Toast
    toastMessageElement.textContent = message;


    // Lấy hoặc tạo Bootstrap Toast instance
    const toast =
        bootstrap.Toast.getOrCreateInstance(
            toastElement
        );


    // Hiển thị Toast
    toast.show();
}

