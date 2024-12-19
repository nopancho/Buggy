export default {
    stories: [
        {
            id: 1,
            title: 'Some fancy name of the Storymap',
            description: 'Some fancy description for this story map'
        },
        {
            id: 2,
            title: 'Some fancy name of the Storymap',
            description: 'Some fancy description for this story map'
        },
        {
            id: 3,
            title: 'Some fancy name of the Storymap',
            description: 'Some fancy description for this story map'
        }
    ],
    init(){
        console.log("init called");
        window.Router.updatePageLinks();
    }
}