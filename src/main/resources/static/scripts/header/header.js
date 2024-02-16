const setActive = () => {
  const path = window.location.pathname
  
  for(element of document.getElementById('mainNavBar').getElementsByClassName('nav-item')) {
    const link = element.getElementsByClassName('nav-link')[0];

    if (link.text.toLowerCase() === 'home') {
      if (path === '/') link.classList.add('active')
    } else if (path.startsWith(link.getAttribute('href'))) {
      link.classList.add('active')
    } else if (link.classList.contains('dropdown-toggle')) {
      const menu = element.getElementsByClassName('dropdown-menu')[0];
      const topLevelLink = menu.getElementsByClassName('dropdown-item')[0];

      if (path.startsWith('/' + topLevelLink.getAttribute('href').split('/')[1])) {
        link.classList.add('active')
      }
    }
  };
}

window.onload = setActive();