// DOM ELEMENTS

// <tbody> dùng để render danh sách Product trên Desktop
const productTableBodyElement = document.getElementById("productTableBody");


// Container dùng để render Product Card trên Mobile
const productMobileListElement = document.getElementById("productMobileList");


// <form> dùng cho cả Add Product và Edit Product
const productFormElement = document.getElementById("productForm");


// Bootstrap Modal dùng cho cả Add Product và Edit Product
const productModalElement = document.getElementById("productModal");


// <div> hiển thị tiêu đề của Modal
// Add Product / Edit Product
const productModalTitleElement = document.getElementById("productModalTitle");


// <select> dùng để chọn Category
const productCategoryInput = document.getElementById("productCategory");


// Hidden <input> dùng để lưu Product ID khi Edit
const productIdInput = document.getElementById("productId");


// <input> nhập tên Product
const productNameInput = document.getElementById("productName");


// <textarea> nhập mô tả Product
const productDescriptionInput = document.getElementById("productDescription");


// <input> nhập giá Product
const productPriceInput = document.getElementById("productPrice");


// <select> chọn Currency
const productCurrencyInput = document.getElementById("productCurrency");


// <input> nhập Stock
const productStockInput = document.getElementById("productStock");


// <input> nhập URL hình ảnh Product
const productImageUrlInput = document.getElementById("productImageUrl");


// <button> dùng cho Add Product / Update Product
const saveProductButtonElement = document.getElementById("saveProductButton");


// Bootstrap Modal dùng để xác nhận Delete Product
const deleteModalElement = document.getElementById("deleteModal");

//
const deleteProductIdElement = document.getElementById("deleteProductId");

const deleteProductIdShowElement = document.getElementById("deleteProductIdShow");



// Element hiển thị tên Product sắp bị xóa
const deleteProductNameElement = document.getElementById("deleteProductName");


// Bootstrap Toast element
const toastElement = document.getElementById("toast");


// Element chứa nội dung thông báo Toast
const toastMessageElement = document.getElementById("toastMessage");


// DATA

// Lưu danh sách Product hiện tại lấy từ Backend
// VARIABLES
let currentProducts = [];

// LOAD CATEGORIES
async function loadCategories() {

    try {

        const response = await fetch("/api/categories");

        if (!response.ok) {
            console.error(
                "Failed to load categories:",
                response.status,
                response.statusText
            );
            return;
        }

        const categories = await response.json();

        renderCategory(categories);

    } catch (error) {

        console.error("Error loading categories:", error);

    }
}


//FUNCTION TO LOAD PRODUCTS

//Function  này có công việc bất đồng bộ async, tra ve tung ket qua mot, tranh bi block trinh duyet do load cung luc
//loadCategories() và loadProducts()  cũng nên có try catch

async function loadProducts() {

    try {

        const response = await fetch("/api/products");

        if (!response.ok) {
            console.error(
                "Failed to load products:",
                response.status,
                response.statusText
            );
            return;
        }

        const products = await response.json();

        currentProducts = products;

        renderDesktopProducts(products);
        renderMobileProducts(products);

    } catch (error) {

        console.error("Error loading products:", error);

    }
}

//RENDER CATEGORY
function renderCategory(categories){
    if(!categories){return};

    // Xóa các option cũ
    productCategoryInput.innerHTML = `
            <option value="">Select Category</option>
        `
   ;

    // Duyệt từng Category
    categories.forEach(category => {

        // Tạo DOM <option></option>
        const option = document.createElement("option");

        // value = categoryId (id trong backend luon ton tai)
        option.value = category.id;

        // Text hiển thị = categoryName
        option.textContent = category.name;

        // Thêm option vào <select>, co method nay moi dua duoc option moi vao select
        productCategoryInput.appendChild(option);
    });
};



//RENDER MOBILE PRODUCTS
function renderMobileProducts(products){
    if(!products){return}
    if(products.length === 0){
        productMobileListElement.innerHTML=`
        <div>
            No products found.
        </div>
        `;
        return;
    }

    productMobileListElement.innerHTML =products.map(product => `

     <!-- PRODUCT MOBILE CARD -->

        <div class="product-mobile-card mb-3 p-3">

            <!-- TOP -->
            <div class="d-flex gap-3 align-items-start">

                <!-- IMAGE -->
                <!-- ${product.imageUrl || ""} , hoac || tra ve chuoi "" rat quan trong, neu khong se tra ve null, ma database dang la not null-->
                <img
                    src="${product.imageUrl || ""}"
                    alt="${escapeHtml(product.name)}"
                    class="product-mobile-image rounded border">


                <!-- PRODUCT INFO -->
                <div class="flex-grow-1">

                    <!-- NAME -->
                    <h6 class="fw-semibold mb-1">

                        ${escapeHtml(product.name)}

                    </h6>


                    <!-- ID -->
                    <div class="small text-secondary mb-2">

                        Product ID: ${product.id}

                    </div>


                    <!-- PRICE -->
                    <div class="text-success fw-semibold mb-2">

                        ${formatPrice(product.price, product.currency)}

                    </div>


                    <!-- STOCK -->
                    <div>

                        ${getStockBadge(product.stock)}

                    </div>

                </div>

            </div>


            <!-- DESCRIPTION -->
            <div class="border-top mt-3 pt-3">

                <div class="small text-secondary">

                    ${truncateText(product.description, 70)}

                </div>

            </div>


            <!-- ACTIONS -->
            <div class="d-flex gap-2 mt-3">


                <!-- EDIT BUTTON - Cach 1: goi onclick trong html-->
                <button
                    type="button"
                    class="btn btn-outline-success flex-grow-1"
                    onclick = "openEditProductModal(${product.id})">

                    Edit

                </button>


                <!-- DELETE BUTTON - Cach 2: khong goi onclick trong html-->
                <button
                    type="button"
                    class="btn btn-outline-danger flex-grow-1"
                    data-action="delete"
                    data-product-id="${product.id}"
                    >

                    Delete

                </button>

            </div>

        </div>


    `).join("");

        //Sau khi HTML duoc render xong, tim tat ca cac button edit, delete
         //Lan nay toi thu lam theo cach thu 2: la tim button delete

         //Tim tat ca button Delete
         //[] trong CSS selector có ý nghĩa là attribute selector — tức là: “hãy tìm element dựa trên thuộc tính HTML”
         const deleteButtons = productMobileListElement.querySelectorAll('[data-action = "delete"]');

         //Gan click event cho tung button Delelte
         deleteButtons.forEach(button => {
            //Gan su kien click vao moi button, roi xu ly theo funtion()
            button.addEventListener("click", function(){
                //Lay doi so productId da luu san o trong button, tuong ung product.id co san trong Table Product do
                const clickProductId = button.dataset.productId;
                //Goi function openDeleteProductModal() da tao trong js nay
                openDeleteProductModal(clickProductId);
            })
         })
}

//RENDER DESKTOP PRODUCTS

function renderDesktopProducts(products){
    if(!products){return}
    if(products.length === 0){
        productTableBodyElement.innerHTML=`
        <div>
            <p>"Not found product"</p>
        </div>
        `;
        return;
    }

    //Lấy mảng products → biến mỗi Product thành một <tr> → ghép tất cả lại → đưa vào <tbody>.
    //product lay ra tu list products tu response cua database
     productTableBodyElement.innerHTML = products.map(product => `

     <tr>

         <!-- PRODUCT -->
         <td class="ps-4">

             <div class="d-flex align-items-center gap-3">

                 <img
                     src="${product.imageUrl || ""}"
                     alt="${escapeHtml(product.name)}"
                     class="rounded border"
                     width="50"
                     height="50"
                 >

                 <div>

                     <div class="fw-semibold">
                         ${escapeHtml(product.name)}
                     </div>

                     <small class="text-secondary">
                         ID: ${product.id}
                     </small>

                 </div>

             </div>

         </td>


         <!-- DESCRIPTION -->
         <td>

             <span class="text-secondary">
                 ${escapeHtml(truncateText(product.description, 70))}
             </span>

         </td>


         <!-- PRICE -->
         <td>

             <span class="text-success fw-semibold">
                 ${formatPrice(product.price, product.currency)}
             </span>

         </td>


         <!-- STOCK -->
         <td>

             ${getStockBadge(product.stock)}

         </td>


         <!-- ACTIONS -->
         <td class="text-end pe-4">

             <!-- EDIT BUTTON - Cach 1: goi onclick trong html-->
             <button
                 type="button"
                 class="btn btn-outline-success btn-sm"
                 onclick = "openEditProductModal(${product.id})">

                 Edit

             </button>

              <!-- DELETE BUTTON - Cach 2: khong goi onclick trong html-->
             <button
                 type="button"
                 class="btn btn-outline-danger btn-sm ms-2"
                 data-action="delete"
                 data-product-id="${product.id}"
                 >

                 Delete

             </button>

         </td>

     </tr>

     `).join("");

     //Sau khi HTML duoc render xong, tim tat ca cac button edit, delete
     //Lan nay toi thu lam theo cach thu 2: la tim button delete

     //Tim tat ca button Delete
     const deleteButtons = productTableBodyElement.querySelectorAll('[data-action = "delete"]');

     //Gan click event cho tung button Delelte
     deleteButtons.forEach(button => {
        //Gan su kien click vao moi button, roi xu ly theo funtion()
        button.addEventListener("click", function(){
            //Lay doi so productId da luu san o trong button, tuong ung product.id co san trong Table Product do
            const clickProductId = button.dataset.productId;
            //Goi function openDeleteProductModal() da tao trong js nay
            openDeleteProductModal(clickProductId);
        })
     })
}


//ADD OR EDIT PRODUCT FORM KHI AN SUBMIT

//await chi dung ben trong async function
productFormElement.addEventListener("submit", async function(event) {

    event.preventDefault();

    // Lấy productId từ hidden input
    const productId = productIdInput.value;

    // Tạo productData từ Form
    const productData = {
        categoryId: productCategoryInput.value, //productCategoryInput.value chinh la categoryId khi truyen value=...
        name: productNameInput.value,
        description: productDescriptionInput.value,
        price: Number(productPriceInput.value),
        currency: productCurrencyInput.value,
        stock: Number(productStockInput.value),
        imageUrl: productImageUrlInput.value || ""
    };

    //try...catch quanh các fetch() để bắt các lỗi kiểu network error, server không phản hồi, JSON lỗi...

    try {

        let response;

        // =========================
        // CREATE
        // =========================
        if (!productId) {

            response = await fetch("/api/products", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify([productData])
            });

            showToast("Product added successfully.", "success");

        }

        // =========================
        // UPDATE
        // =========================
        else {

            response = await fetch(`/api/products/${productId}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(productData)
            });
            showToast("Product updated successfully.", "success");

        }

        // =========================
        // CHECK RESPONSE
        // =========================

        if (!response.ok) {

            console.error(
                "Request failed:",
                response.status,
                response.statusText
            );

            showToast("Failed to save product","danger");
            return;
        }

        // =========================
        // RELOAD PRODUCTS
        // =========================

        await loadProducts();

        // =========================
        // CLOSE MODAL
        // =========================

        const modal =
            bootstrap.Modal.getOrCreateInstance(productModal);

        modal.hide();

    } catch (error) {

        console.error("Error saving product:", error);

        showToast("Something went wrong. Please try again.","warning");

    }

});

// OPEN ADD PRODUCT MODAL
window.openAddProductModal = function(){

    //Doi tieu de Modal
    productModalTitleElement.textContent = "Add Product";

    //Doi ten nut Save
    saveProductButtonElement.textContent = "Add Product"

    //Reset toan bo Form
    productFormElement.reset();

    //Xoa Product Id
    productIdInput.value = "";

    //Lay hoac tao bootstrap modal instance
    const modal = bootstrap.Modal.getOrCreateInstance(productModal);

    //Hien thi Modal
    modal.show();

}

//OPEN EDIT PRODUCT MODAL
window.openEditProductModal = function(productId){

    // Tim san pham -> product.id (tra ve tu post api) = productId ( da luu san trong onclick method, productId trong table đã lưu sẵn ở mỗi phần tử product được render lên)
    // Tìm trong danh sách currentProducts sản phẩm có id trùng với productId
    const product = currentProducts.find(product => product.id == Number(productId));

    //Check khong ton tai product
    if(!product){
        showToast("Product not found","warning");
        return;
    }

    //Luu ID product dang edit vao VALUE hidden input --> <input type="hidden" id="productId" value="5">, lun nay
    //sau khi modal bat len, modal moi biet duoc dang edit cho product co id = bao nhieu
    //tham so productId truyen vao tu openEditProductModal = function(productId){}, chi la tham so truyen vao de lay product.id that ra ngoai
    //chu luc nay edit modal hoan toan chua luu productId VALUE
    productIdInput.value = product.id;

    //Doi tieu de Modal
    productModalTitleElement.textContent = "Edit Product";

    //Doi ten nut Save
    saveProductButtonElement.textContent = "Update Product";



    //Show toan bo Form Detail da dien san, tuong ung voi id, value "" chinh la bang categoryId

    productCategoryInput.value = product.categoryId;

    productNameInput.value = product.name;
    productDescriptionInput.value = product.description;
    productPriceInput.value = Number(product.price);
    productCurrencyInput.value = product.currency;
    productStockInput.value = Number(product.stock);
    productImageUrlInput.value = product.imageUrl || "";

    //Lay hoac tao bootstrap modal instance
    const modal = bootstrap.Modal.getOrCreateInstance(productModalElement);

    //Hien thi Modal
    modal.show();

}


//OPEN DELETE PRODUCT MODAL
function openDeleteProductModal(productId) {

    // Tìm product trong currentProducts có id trùng với productId
    const product = currentProducts.find(
        product => product.id === Number(productId)
    );

    // Không tìm thấy product
    if (!product) {
        showToast("Product not found","warning");
        return;
    }

    // Lưu ID product đang chuẩn bị xóa
    deleteProductIdElement.value = product.id;

    // Hiển thị tên product trong Delete Modal
    deleteProductNameElement.textContent = product.name;
    deleteProductIdShowElement.textContent = ` (ID = ${product.id})`;

    // Lấy hoặc tạo Bootstrap Modal instance
    const deleteModal =
        bootstrap.Modal.getOrCreateInstance(
            deleteModalElement
        );

    // Hiển thị Delete Modal
    deleteModal.show();
}


//CONFIRM DELETE BUTTON addEventListener CLICK


// =========================
// CONFIRM DELETE BUTTON
// =========================

const confirmDeleteButton =
    deleteModalElement.querySelector(
        '[data-action="confirmDelete"]'
    );

confirmDeleteButton.addEventListener("click", function () {

    confirmDelete();

});

// CONFITM DELETE FUNCTION
async function confirmDelete() {

    // Lấy Product ID đang được lưu trong hidden input
    const deleteProductId =
        deleteProductIdElement.value;


    try {

        // Gửi DELETE request đến Backend
        const response = await fetch(
            `/api/products/${deleteProductId}`,
            {
                method: "DELETE"
            }
        );


        // Kiểm tra HTTP response
        if (!response.ok) {

            console.error(
                "Failed to delete product:",
                response.status,
                response.statusText
            );

            showToast("Failed to delete product","warning");

            return;
        }

        await loadProducts();


        // DELETE thành công
        showToast("Product deleted successfully","danger")

        //Dong Delete Modal

        const deleteModal = bootstrap.Modal.getOrCreateInstance(deleteModalElement);

        deleteModal.hide();

    } catch (error) {

        // Network error / fetch error
        console.error(
            "Error deleting product:",
            error
        );

    }

}







//ESCAPE HTML

function escapeHtml (value){
    // Nếu value là null, undefined, "", 0, false... thì trả về chuỗi rỗng ""
    if(value == null){return ""}

    // Tạo một DOM element <div> tạm thời
    const div = document.createElement("div");
    // Đưa value vào dưới dạng text thuần, textContent không xử lý value như HTML
    div.textContent = value;
    // Lấy nội dung HTML bên trong <div>, các ký tự HTML đặc biệt sẽ được escape
    return div.innerHTML;
}

//TRUNCATE TEXT

function truncateText(text, maxLength) {

    // Nếu không có nội dung text thì trả về chuỗi rỗng
    if (!text) {
        return "";
    }

    // Nếu độ dài text nhỏ hơn hoặc bằng độ dài tối đa thì giữ nguyên text
    if (text.length <= maxLength) {
        return text;
    }

    // Nếu text dài hơn độ dài tối đa  thì cắt text và thêm "..."
    return text.substring(0, maxLength) + "...";

}

//FORMAT PRICE
function formatPrice(price, currency){

    return new Intl.NumberFormat(
        "vi-VN",{
            //Hien thi theo dang tien te
            style:"currency",

            //Dung currency duoc truyen vao, man dinh la VND
            currency: currency || "VND",

            //Khong hien thi so thap phan
            maximumFractionDigits: 0

        }

    ).format(price);
}

//GET STOCK BADGE
// Kiểm tra stock và trả về HTML tương ứng
function getStockBadge(stock) {

    // Hết hàng
    if (stock <= 0) {
        return `
            <span class = "text-danger">Out of stock</span>
        `;
    }

    // Sắp hết hàng
    if (stock <= 5) {
        return `
            <span class = "text-warning">${stock} left</span>
        `;
    }

    // Còn nhiều hàng
    return `
        <span class = "text-success">${stock} in stock</span>
    `;
}

//FUNCTION SHOW TOAST

//HIen thi Toast voi noi dung message duoc truyen vao

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

//INITIAL LOAD

//function nay de load du lieu tu backend ve frontend de bind vao html
document.addEventListener("DOMContentLoaded", async function (){
    //loadCategories() phải chạy trước khi openEditProductModal() được gọi
    await loadCategories();
    await loadProducts();

});


