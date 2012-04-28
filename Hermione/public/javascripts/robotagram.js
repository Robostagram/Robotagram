(function(){
        function keypressHandler(event){
            var key = e.which;
            alert(key);
        }
    $(document).ready(function(){
        $(window).keypress(keypressHandler);
    })
})();