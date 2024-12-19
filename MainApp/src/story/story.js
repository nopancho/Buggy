import mapboxgl from 'mapbox-gl';
export default {
    test:"hallo",
    storyId: window.Alpine.store("routeData").data.storyId,
    init(){
        console.log(window.Alpine.store("routeData").data);
        mapboxgl.accessToken = 'pk.eyJ1Ijoibm9wYW5jaG8iLCJhIjoiY20yZHZkcDVxMHA5dDJxc2ZncGxxaHFyYiJ9.jfyqD3DptYUM07GmcA6QGA';
// Initialize Mapbox
        const map = new mapboxgl.Map({
            container: 'map',
            style: 'mapbox://styles/nopancho/cm2g07shq006f01pe2s8p9nx3',
            center: [0, 0], // Initial center [lng, lat]
            zoom: 2,        // Initial zoom level
        });
    }
}